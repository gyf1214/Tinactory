package org.shsts.tinactory.core.machine;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllCapabilities;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.metrics.MetricsManager;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.DistLazy;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;
import static org.shsts.tinactory.core.util.CodecHelper.encodeList;
import static org.shsts.tinactory.core.util.CodecHelper.parseList;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor extends CapabilityProvider implements
    IMachineProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final BlockEntity blockEntity;
    private final List<IRecipeProcessor<?>> processors;
    private final boolean autoRecipe;
    private boolean stopped = false;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    private int processorIndex;
    private final List<ProcessingInfo> infoList = new ArrayList<>();
    private final ListMultimap<Integer, IProcessingObject> infoMap = ArrayListMultimap.create();

    private record ProcessorRecipe<T>(int index, IRecipeProcessor<T> processor, T recipe) {
        public void onWorkBegin(IMachine machine, int maxParallel, List<ProcessingInfo> infoList) {
            infoList.clear();
            processor.onWorkBegin(recipe, machine, maxParallel, info -> {
                infoList.add(info);
                var ingredient = info.object();
                if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
                    MetricsManager.reportItem("item_consumed", machine, item.stack());
                } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                    MetricsManager.reportFluid("fluid_consumed", machine, fluid.fluid());
                }
            });
        }

        public void onWorkContinue(IMachine machine) {
            processor.onWorkContinue(recipe, machine);
        }

        public long onWorkProcess(double partial) {
            return processor.onWorkProgress(recipe, partial);
        }

        public void onWorkDone(IMachine machine, Random random) {
            processor.onWorkDone(recipe, machine, random, result -> {
                if (result instanceof ProcessingResults.ItemResult item) {
                    MetricsManager.reportItem("item_produced", machine, item.stack);
                } else if (result instanceof ProcessingResults.FluidResult fluid) {
                    MetricsManager.reportFluid("fluid_produced", machine, fluid.stack);
                }
            });
        }

        public long maxProcess() {
            return processor.getMaxWorkProgress(recipe);
        }

        public ResourceLocation loc() {
            return processor.toLoc(recipe);
        }

        public ElectricMachineType machineType() {
            return processor.electricMachineType(recipe);
        }

        public double powerGen() {
            return processor.powerGen(recipe);
        }

        public double powerCons() {
            return processor.powerCons(recipe);
        }
    }

    @Nullable
    private ProcessorRecipe<?> currentRecipe = null;
    private boolean needUpdate = true;

    private long workProgress = 0;

    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;

    public MachineProcessor(BlockEntity blockEntity, Collection<? extends IRecipeProcessor<?>> processors,
        boolean autoRecipe) {
        this.blockEntity = blockEntity;
        this.processors = new ArrayList<>(processors);
        this.autoRecipe = autoRecipe;
    }

    private Level world() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    protected Optional<IMachine> machine() {
        return MACHINE.tryGet(blockEntity);
    }

    private Optional<IContainer> container() {
        return machine().flatMap(IMachine::container);
    }

    /**
     * Return max parallel for the processor.
     */
    protected int maxParallel() {
        return 1;
    }

    private Optional<ResourceLocation> targetRecipe() {
        return machine().flatMap($ -> $.config().getLoc("targetRecipe"));
    }

    public DistLazy<List<IRecipeBookItem>> targetRecipes() {
        var machine = machine();
        if (machine.isEmpty()) {
            return () -> Collections::emptyList;
        }
        var world = world();
        return () -> () -> {
            var ret = new ArrayList<IRecipeBookItem>();
            for (var processor : processors) {
                var items = processor.recipeBookItems(world, machine.get()).getValue();
                ret.addAll(items);
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
                    case ITEM -> port.asItemFilter().resetFilters();
                    case FLUID -> port.asFluidFilter().resetFilters();
                }
            }
        });
    }

    private void setTargetRecipe(ResourceLocation loc) {
        var world = world();
        var machine = machine().orElseThrow();

        // first clear the filter
        clearFilters(PortDirection.INPUT);
        for (var processor : processors) {
            if (processor.allowTargetRecipe(world, loc, machine)) {
                LOGGER.debug("{}: update target recipe = {}", blockEntity, loc);
                processor.setTargetRecipe(world, loc, machine);
                return;
            }
        }
        // no processor can handle this target recipe
    }

    private void resetTargetRecipe() {
        LOGGER.debug("{}: update target recipe = <null>", blockEntity);
        clearFilters(PortDirection.INPUT);
    }

    private void updateTargetRecipe() {
        targetRecipe().ifPresentOrElse(this::setTargetRecipe, this::resetTargetRecipe);
    }

    private <T> boolean newRecipe(int index, IRecipeProcessor<T> processor, Level world,
        IMachine machine, Optional<ResourceLocation> target) {
        if (!autoRecipe && target.isEmpty()) {
            return false;
        }
        var recipe = processor.newRecipe(world, machine, target);
        // newRecipe may set outputFilters, we clear it now.
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

    @Override
    public Optional<IProcessingObject> getInfo(int port, int index) {
        var list = infoMap.get(port);
        return index >= 0 && index < list.size() ? Optional.ofNullable(list.get(index)) :
            Optional.empty();
    }

    private void setUpdateRecipe() {
        if (currentRecipe == null) {
            needUpdate = true;
        }
    }

    private void onTechChange(ITeamProfile team) {
        if (team == machine().flatMap(IMachine::owner).orElse(null)) {
            setUpdateRecipe();
        }
    }

    @Override
    public void onPreWork() {
        if (currentRecipe != null || !needUpdate) {
            return;
        }

        if (stopped) {
            workProgress = 0;
            stopped = false;
            return;
        }

        var world = world();
        assert currentRecipeLoc == null;
        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }

        var target = targetRecipe();
        for (var i = 0; i < processors.size(); i++) {
            var processor = processors.get(i);
            if (newRecipe(i, processor, world, machine.get(), target)) {
                break;
            }
        }

        workProgress = 0;
        if (currentRecipe != null) {
            currentRecipe.onWorkBegin(machine.get(), maxParallel(), infoList);
            buildInfoMap();
        }
        needUpdate = false;
        blockEntity.setChanged();
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

        var progress = currentRecipe.onWorkProcess(partial);
        workProgress += progress;
        if (workProgress >= currentRecipe.maxProcess()) {
            currentRecipe.onWorkDone(machine.get(), world().random);
            // onWorkDone may set outputFilters, we clear it now.
            clearFilters(PortDirection.OUTPUT);
            currentRecipe = null;
            infoList.clear();
            infoMap.clear();
            needUpdate = true;
        }
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        if (currentRecipe == null) {
            return workProgress > 0 ? 1d : 0d;
        }
        return (double) workProgress / (double) currentRecipe.maxProcess();
    }

    @Override
    public long getVoltage() {
        return machine().map($ -> getBlockVoltage($.blockEntity()).value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return currentRecipe == null ? ElectricMachineType.NONE : currentRecipe.machineType();
    }

    @Override
    public double getPowerGen() {
        return currentRecipe == null ? 0 : currentRecipe.powerGen();
    }

    @Override
    public double getPowerCons() {
        return currentRecipe == null ? 0 : currentRecipe.powerCons();
    }

    private <T> void recoverRecipe(int index, IRecipeProcessor<T> processor,
        Level world, ResourceLocation loc) {
        processor.byLoc(world, loc).ifPresent(recipe ->
            currentRecipe = new ProcessorRecipe<>(index, processor, recipe));
    }

    private void onServerLoad(Level world) {
        currentRecipe = null;
        if (currentRecipeLoc != null) {
            var processor = processors.get(processorIndex);
            recoverRecipe(processorIndex, processor, world, currentRecipeLoc);
            currentRecipeLoc = null;
        }

        if (currentRecipe != null) {
            machine().ifPresent(currentRecipe::onWorkContinue);
            needUpdate = false;
        }

        updateTargetRecipe();
        TechManager.server().onProgressChange(onTechChange);
    }

    private void onConnect(INetwork network) {
        machine().ifPresent(machine -> Machine.registerStopSignal(network, machine, $ -> stopped = $));
    }

    private void onRemoved(Level world) {
        if (!world.isClientSide) {
            TechManager.server().removeProgressChangeListener(onTechChange);
        }
    }

    private void onMachineConfig() {
        updateTargetRecipe();
        setUpdateRecipe();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), this::onServerLoad);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(CONTAINER_CHANGE.get(), this::setUpdateRecipe);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (currentRecipe != null) {
            tag.putString("currentRecipe", currentRecipe.loc().toString());
            tag.putInt("processorIndex", currentRecipe.index());
            tag.putLong("workProgress", workProgress);
            tag.put("processorData", currentRecipe.processor().serializeNBT());
            tag.put("processorInfo", encodeList(infoList, ProcessingInfo::serializeNBT));
        } else if (currentRecipeLoc != null) {
            tag.putString("currentRecipe", currentRecipeLoc.toString());
            tag.putInt("processorIndex", processorIndex);
            tag.putLong("workProgress", workProgress);
            tag.put("processorData", processors.get(processorIndex).serializeNBT());
            tag.put("processorInfo", encodeList(infoList, ProcessingInfo::serializeNBT));
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
            var data = tag.getCompound("processorData");
            var processor = processors.get(processorIndex);
            processor.deserializeNBT(data);
            parseList(tag.getList("processorInfo", Tag.TAG_COMPOUND),
                $ -> ProcessingInfo.fromNBT((CompoundTag) $), infoList::add);
            buildInfoMap();
        } else {
            currentRecipeLoc = null;
            infoMap.clear();
        }
    }
}
