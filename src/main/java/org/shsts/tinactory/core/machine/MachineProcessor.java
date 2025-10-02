package org.shsts.tinactory.core.machine;

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
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.Tuple;
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

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor extends CapabilityProvider implements
    IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final BlockEntity blockEntity;
    private final List<IRecipeProcessor<?>> processors;
    private final boolean autoRecipe;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;

    private record ProcessorRecipe<T>(IRecipeProcessor<T> processor, T recipe) {
        public void onWorkBegin(IMachine machine) {
            processor.onWorkBegin(recipe, machine);
        }

        public void onWorkContinue(IMachine machine) {
            processor.onWorkContinue(recipe, machine);
        }

        public long onWorkProcess(double partial) {
            return processor.onWorkProgress(recipe, partial);
        }

        public void onWorkDone(IMachine machine, Random random) {
            processor.onWorkDone(recipe, machine, random);
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
            var ret = new ArrayList<Tuple<Integer, IRecipeBookItem>>();
            var i = 0;
            for (var processor : processors) {
                var items = processor.recipeBookItems(world, machine.get()).getValue();
                for (var item : items) {
                    ret.add(new Tuple<>(i, item));
                }
                i++;
            }

            ret.sort(Comparator.comparing(Tuple<Integer, IRecipeBookItem>::first)
                .thenComparing($ -> !$.second().isMarker())
                .thenComparing($ -> $.second().loc(), ResourceLocation::compareNamespaced));

            return ret.stream().map(Tuple::second).toList();
        };
    }

    private void setTargetRecipe(ResourceLocation loc) {
        var world = world();
        var machine = machine().orElseThrow();

        for (var processor : processors) {
            if (processor.allowTargetRecipe(world, loc, machine)) {
                LOGGER.debug("{}: update target recipe = {}", blockEntity, loc);
                processor.setTargetRecipe(world, loc, machine);
                return;
            }
        }
        /* No processor can handle this target recipe */
        resetTargetRecipe();
    }

    private void resetTargetRecipe() {
        LOGGER.debug("{}: update target recipe = <null>", blockEntity);
        container().ifPresent(container -> {
            var portSize = container.portSize();
            for (var i = 0; i < portSize; i++) {
                if (!container.hasPort(i) || container.portDirection(i) != PortDirection.INPUT) {
                    continue;
                }
                var port = container.getPort(i, true);
                switch (port.type()) {
                    case ITEM -> port.asItemFilter().resetFilters();
                    case FLUID -> port.asFluidFilter().resetFilters();
                }
            }
        });
    }

    private void updateTargetRecipe() {
        targetRecipe().ifPresentOrElse(this::setTargetRecipe, this::resetTargetRecipe);
    }

    private <T> boolean newRecipe(IRecipeProcessor<T> processor, Level world,
        IMachine machine, Optional<ResourceLocation> target) {
        if (!autoRecipe && target.isEmpty()) {
            return false;
        }
        var recipe = processor.newRecipe(world, machine, target);
        if (recipe.isPresent()) {
            currentRecipe = new ProcessorRecipe<>(processor, recipe.get());
            return true;
        }
        return false;
    }

    private void updateRecipe() {
        if (currentRecipe != null || !needUpdate) {
            return;
        }
        var world = world();
        assert currentRecipeLoc == null;
        var machine = machine();
        if (machine.isEmpty()) {
            return;
        }

        var target = targetRecipe();
        for (var processor : processors) {
            if (newRecipe(processor, world, machine.get(), target)) {
                break;
            }
        }

        workProgress = 0;
        if (currentRecipe != null) {
            currentRecipe.onWorkBegin(machine.get());
        }
        needUpdate = false;
        blockEntity.setChanged();
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
        updateRecipe();
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
            currentRecipe = null;
            needUpdate = true;
        }
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        if (currentRecipe == null) {
            return 0;
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

    private <T> boolean recoverRecipe(IRecipeProcessor<T> processor, Level world, ResourceLocation loc) {
        var recipe = processor.byLoc(world, loc);
        if (recipe.isPresent()) {
            currentRecipe = new ProcessorRecipe<>(processor, recipe.get());
            return true;
        }
        return false;
    }

    private void onServerLoad(Level world) {
        if (currentRecipeLoc != null) {
            for (var processor : processors) {
                if (recoverRecipe(processor, world, currentRecipeLoc)) {
                    break;
                }
            }
        }
        currentRecipeLoc = null;

        if (currentRecipe != null) {
            machine().ifPresent(currentRecipe::onWorkContinue);
            needUpdate = false;
        }

        updateTargetRecipe();
        TechManager.server().onProgressChange(onTechChange);
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
            tag.putLong("workProgress", workProgress);
        } else if (currentRecipeLoc != null) {
            tag.putString("currentRecipe", currentRecipeLoc.toString());
            tag.putLong("workProgress", workProgress);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        currentRecipe = null;
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            currentRecipeLoc = new ResourceLocation(tag.getString("currentRecipe"));
            workProgress = tag.getLong("workProgress");
        } else {
            currentRecipeLoc = null;
        }
    }
}
