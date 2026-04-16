package org.shsts.tinactory.core.machine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.CodecHelper.encodeTag;
import static org.shsts.tinactory.core.util.CodecHelper.encodeList;
import static org.shsts.tinactory.core.util.CodecHelper.parseTag;
import static org.shsts.tinactory.core.util.CodecHelper.parseList;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRuntime implements IMachineProcessor, INBTSerializable<CompoundTag> {
    private final List<IRecipeProcessor<?>> processors;
    private final boolean autoRecipe;
    private final Supplier<Optional<IMachine>> machineSupplier;
    private final boolean isClientSide;
    private final Runnable onUpdate;
    private final Codec<ProcessingInfo> processingInfoCodec;
    private final List<ProcessingInfo> infoList = new ArrayList<>();
    private final ListMultimap<Integer, IProcessingObject> infoMap = ArrayListMultimap.create();
    private boolean stopped = false;
    private boolean firstConnect = false;
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    private int processorIndex;
    private double workSpeed;
    @Nullable
    private ProcessorRecipe<?> currentRecipe = null;
    private boolean needUpdate = true;
    private long workProgress = 0L;

    private record ProcessorRecipe<T>(int index, IRecipeProcessor<T> processor, T recipe) {
        public void onWorkBegin(IMachine machine, int maxParallel, List<ProcessingInfo> infoList) {
            infoList.clear();
            processor.onWorkBegin(recipe, machine, maxParallel, infoList::add);
        }

        public void onWorkContinue(IMachine machine) {
            processor.onWorkContinue(recipe, machine);
        }

        public long onWorkProcess(double partial) {
            return processor.onWorkProgress(recipe, partial);
        }

        public void onWorkDone(IMachine machine, Random random) {
            // TODO revisit machine metrics boundary after runtime extraction settles.
            processor.onWorkDone(recipe, machine, random, result -> {});
        }

        public long maxProgress() {
            return processor.maxWorkProgress(recipe);
        }

        public long maxProgressTicks() {
            return Math.max(1, processor.workTicksFromProgress(processor.maxWorkProgress(recipe)));
        }

        public ResourceLocation loc() {
            return processor.toLoc(recipe);
        }
    }

    public ProcessingRuntime(Collection<? extends IRecipeProcessor<?>> processors,
        boolean autoRecipe, Supplier<Optional<IMachine>> machineSupplier, boolean isClientSide,
        Runnable onUpdate, Codec<ProcessingInfo> processingInfoCodec) {
        this.processors = List.copyOf(processors);
        this.autoRecipe = autoRecipe;
        this.machineSupplier = machineSupplier;
        this.isClientSide = isClientSide;
        this.onUpdate = onUpdate;
        this.processingInfoCodec = processingInfoCodec;
    }

    private Optional<IMachine> machine() {
        return machineSupplier.get();
    }

    private Optional<IContainer> container() {
        return machine().flatMap(IMachine::container);
    }

    private Optional<ResourceLocation> targetRecipe() {
        return machine().flatMap($ -> $.config().getLoc("targetRecipe"));
    }

    @Override
    public DistLazy<List<IRecipeBookItem>> recipeBookItems() {
        var machine = machine();
        if (machine.isEmpty()) {
            return () -> Collections::emptyList;
        }
        return () -> () -> {
            var ret = new ArrayList<IRecipeBookItem>();
            for (var processor : processors) {
                ret.addAll(processor.recipeBookItems(machine.get()).getValue());
            }
            ret.sort(Comparator.comparing($ -> !$.isMarker()));
            return ret;
        };
    }

    private void clearFilters(PortDirection direction) {
        container().ifPresent(container -> {
            var portSize = container.portSize();
            for (var i = 0; i < portSize; i++) {
                if (!container.hasPort(i) || container.portDirection(i) != direction) {
                    continue;
                }
                var port = container.getPort(i, ContainerAccess.INTERNAL);
                switch (port.type()) {
                    case ITEM -> port.asItem().asFilter().resetFilters();
                    case FLUID -> port.asFluid().asFilter().resetFilters();
                }
            }
        });
    }

    private void setTargetRecipe(ResourceLocation loc) {
        var machine = machine().orElseThrow();
        clearFilters(PortDirection.INPUT);
        for (var processor : processors) {
            if (processor.allowTargetRecipe(isClientSide, loc, machine)) {
                processor.setTargetRecipe(loc, machine);
                return;
            }
        }
    }

    private void resetTargetRecipe() {
        clearFilters(PortDirection.INPUT);
    }

    private void updateTargetRecipe() {
        targetRecipe().ifPresentOrElse(this::setTargetRecipe, this::resetTargetRecipe);
    }

    private <T> boolean newRecipe(int index, IRecipeProcessor<T> processor, IMachine machine,
        Optional<ResourceLocation> target) {
        if (!autoRecipe && target.isEmpty()) {
            return false;
        }
        var recipe = processor.newRecipe(machine, target);
        clearFilters(PortDirection.OUTPUT);
        if (recipe.isPresent()) {
            currentRecipe = new ProcessorRecipe<>(index, processor, recipe.get());
            return true;
        }
        return false;
    }

    private void buildInfoMap() {
        infoMap.clear();
        for (var info : infoList) {
            infoMap.put(info.port(), info.object());
        }
    }

    private void setUpdateRecipe() {
        if (currentRecipe == null) {
            needUpdate = true;
        }
    }

    public void onContainerChange() {
        setUpdateRecipe();
    }

    public void onMachineConfig() {
        updateTargetRecipe();
        setUpdateRecipe();
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    private <T> void recoverRecipe(int index, IRecipeProcessor<T> processor, ResourceLocation loc) {
        processor.byLoc(loc).ifPresent(recipe ->
            currentRecipe = new ProcessorRecipe<>(index, processor, recipe));
    }

    private void onFirstConnect() {
        currentRecipe = null;
        if (currentRecipeLoc != null) {
            var processor = processors.get(processorIndex);
            recoverRecipe(processorIndex, processor, currentRecipeLoc);
            currentRecipeLoc = null;
        }
        if (currentRecipe != null) {
            machine().ifPresent(currentRecipe::onWorkContinue);
            needUpdate = false;
        }
        updateTargetRecipe();
        firstConnect = true;
    }

    public void onConnect() {
        if (!firstConnect) {
            onFirstConnect();
        }
    }

    @Override
    public void onPreWork() {
        if (!firstConnect) {
            onFirstConnect();
        }
        if (currentRecipe != null) {
            if (workProgress >= currentRecipe.maxProgress()) {
                currentRecipe = null;
                infoList.clear();
                infoMap.clear();
                needUpdate = true;
            } else {
                return;
            }
        }
        if (!needUpdate) {
            return;
        }
        workProgress = 0;
        if (stopped) {
            return;
        }
        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }
        var target = targetRecipe();
        for (var i = 0; i < processors.size(); i++) {
            var processor = processors.get(i);
            if (newRecipe(i, processor, machine.get(), target)) {
                break;
            }
        }
        if (currentRecipe != null) {
            currentRecipe.onWorkBegin(machine.get(), machine.get().parallel(), infoList);
            buildInfoMap();
        }
        needUpdate = false;
        onUpdate.run();
    }

    @Override
    public void onWorkTick(double partial) {
        if (currentRecipe == null) {
            return;
        }
        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }
        workProgress += currentRecipe.onWorkProcess(partial);
        workSpeed = currentRecipe.processor.workSpeed(partial);
        if (workProgress >= currentRecipe.maxProgress()) {
            currentRecipe.onWorkDone(machine.get(), machine.get().random());
            clearFilters(PortDirection.OUTPUT);
            workProgress = currentRecipe.maxProgress();
        }
        onUpdate.run();
    }

    @Override
    public Optional<IProcessingObject> getInfo(int port, int index) {
        var list = infoMap.get(port);
        return index >= 0 && index < list.size() ? Optional.ofNullable(list.get(index)) :
            Optional.empty();
    }

    @Override
    public List<IProcessingObject> getAllInfo() {
        return infoList.stream().map(ProcessingInfo::object).toList();
    }

    @Override
    public long progressTicks() {
        return currentRecipe == null ? 0 : currentRecipe.processor().workTicksFromProgress(workProgress);
    }

    @Override
    public long maxProgressTicks() {
        return currentRecipe == null ? 0 : currentRecipe.maxProgressTicks();
    }

    @Override
    public double workSpeed() {
        return currentRecipe == null ? -1d : workSpeed;
    }

    @Override
    public boolean supportsRecipeType(ResourceLocation recipeTypeId) {
        for (var processor : processors) {
            if (processor.recipeTypeId().equals(recipeTypeId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWorking(double partial) {
        return currentRecipe != null && workSpeed > 0;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (currentRecipe != null) {
            tag.putString("currentRecipe", currentRecipe.loc().toString());
            tag.putInt("processorIndex", currentRecipe.index());
            tag.putLong("workProgress", workProgress);
            tag.put("processorData", currentRecipe.processor().serializeNBT());
            tag.put("processorInfo", encodeList(infoList,
                info -> encodeTag(processingInfoCodec, info)));
        } else if (currentRecipeLoc != null) {
            tag.putString("currentRecipe", currentRecipeLoc.toString());
            tag.putInt("processorIndex", processorIndex);
            tag.putLong("workProgress", workProgress);
            tag.put("processorData", processors.get(processorIndex).serializeNBT());
            tag.put("processorInfo", encodeList(infoList,
                info -> encodeTag(processingInfoCodec, info)));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        currentRecipe = null;
        infoList.clear();
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            currentRecipeLoc = new ResourceLocation(tag.getString("currentRecipe"));
            processorIndex = tag.getInt("processorIndex");
            workProgress = tag.getLong("workProgress");
            processors.get(processorIndex).deserializeNBT(tag.getCompound("processorData"));
            parseList(tag.getList("processorInfo", Tag.TAG_COMPOUND),
                value -> parseTag(processingInfoCodec, value), infoList::add);
            buildInfoMap();
        } else {
            currentRecipeLoc = null;
            infoMap.clear();
        }
    }
}
