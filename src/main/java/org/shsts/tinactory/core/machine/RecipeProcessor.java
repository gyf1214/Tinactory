package org.shsts.tinactory.core.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.electric.GeneratorProcessor;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.ElectricFurnace;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.content.machine.OreAnalyzerProcessor;
import org.shsts.tinactory.content.multiblock.MultiBlockProcessor;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeProcessor<T extends Recipe<?>> extends CapabilityProvider implements
        IProcessor, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final long PROGRESS_PER_TICK = 256;

    protected final BlockEntity blockEntity;
    public final RecipeType<? extends T> recipeType;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    @Nullable
    protected T currentRecipe = null;
    @Nullable
    protected T targetRecipe = null;
    private boolean needUpdate = true;
    protected long workProgress = 0;

    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;

    protected RecipeProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
    }

    protected Level getWorld() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    protected abstract boolean matches(Level world, T recipe, IContainer container);

    protected abstract List<? extends T> getMatchedRecipes(Level world, IContainer container);

    protected Optional<T> getNewRecipe(Level world, IContainer container) {
        if (targetRecipe != null) {
            if (matches(world, targetRecipe, container)) {
                return Optional.of(targetRecipe);
            }
        } else {
            var matches = getMatchedRecipes(world, container);
            if (matches.size() == 1) {
                return Optional.of(matches.get(0));
            }
        }
        return Optional.empty();
    }

    private void updateRecipe() {
        if (currentRecipe != null || !needUpdate) {
            return;
        }
        var world = getWorld();
        assert currentRecipeLoc == null;
        var container = getContainer().orElseThrow();
        currentRecipe = getNewRecipe(world, container).orElse(null);
        workProgress = 0;
        if (currentRecipe != null) {
            onWorkBegin(currentRecipe, container);
        }
        needUpdate = false;
        blockEntity.setChanged();
    }

    public void setUpdateRecipe() {
        if (currentRecipe == null) {
            needUpdate = true;
        }
    }

    protected abstract boolean allowTargetRecipe(Recipe<?> recipe);

    protected abstract void doSetTargetRecipe(Recipe<?> recipe);

    public void setTargetRecipe(ResourceLocation loc) {
        var recipeManager = getWorld().getRecipeManager();

        var recipe = recipeManager.byKey(loc).orElse(null);
        if (recipe == null || !allowTargetRecipe(recipe)) {
            resetTargetRecipe();
            return;
        }

        LOGGER.debug("update target recipe = {}", loc);
        doSetTargetRecipe(recipe);
    }

    public void resetTargetRecipe() {
        LOGGER.debug("update target recipe = <null>");
        targetRecipe = null;
        getContainer().ifPresent(container -> {
            var portSize = container.portSize();
            for (var i = 0; i < portSize; i++) {
                if (!container.hasPort(i) || container.portDirection(i) != PortDirection.INPUT) {
                    continue;
                }
                var port = container.getPort(i, false);
                switch (port.type()) {
                    case ITEM -> port.asItem().resetItemFilter();
                    case FLUID -> port.asFluid().resetFluidFilter();
                }
            }
        });
    }

    private void updateTargetRecipe() {
        var recipe = AllCapabilities.MULTI_BLOCK.tryGet(blockEntity)
                .flatMap(MultiBlock::getInterface)
                .map($ -> (Machine) $)
                .or(() -> AllCapabilities.MACHINE.tryGet(blockEntity))
                .flatMap($ -> $.config.getLoc("targetRecipe"));

        recipe.ifPresentOrElse(this::setTargetRecipe, this::resetTargetRecipe);
    }

    protected abstract void onWorkBegin(T recipe, IContainer container);

    protected void onWorkContinue(T recipe) {}

    protected abstract long onWorkProgress(T recipe, double partial);

    protected abstract void onWorkDone(T recipe, IContainer container, Random random);

    protected abstract long getMaxWorkProgress(T recipe);

    @Override
    public void onPreWork() {
        updateRecipe();
    }

    @Override
    public void onWorkTick(double partial) {
        if (currentRecipe == null) {
            return;
        }
        var container = getContainer().orElseThrow();
        var progress = onWorkProgress(currentRecipe, partial);
        workProgress += progress;
        if (workProgress >= getMaxWorkProgress(currentRecipe)) {
            onWorkDone(currentRecipe, container, getWorld().random);
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
        return (double) workProgress / (double) getMaxWorkProgress(currentRecipe);
    }

    private void onTechChange(ITeamProfile team) {
        if (team == getContainer().flatMap(IContainer::getOwnerTeam).orElse(null)) {
            setUpdateRecipe();
        }
    }

    protected Optional<IContainer> getContainer() {
        return AllCapabilities.MULTI_BLOCK.tryGet(blockEntity)
                .flatMap(MultiBlock::getContainer)
                .or(() -> AllCapabilities.CONTAINER.tryGet(blockEntity));
    }

    @SuppressWarnings("unchecked")
    public void onServerLoad(Level world) {
        var recipeManager = world.getRecipeManager();
        currentRecipe = (T) Optional.ofNullable(currentRecipeLoc)
                .flatMap(recipeManager::byKey)
                .filter(r -> r.getType() == recipeType)
                .orElse(null);
        currentRecipeLoc = null;
        if (currentRecipe != null) {
            onWorkContinue(currentRecipe);
            needUpdate = false;
        }

        updateTargetRecipe();
        TechManager.server().onProgressChange(onTechChange);
    }

    public void onRemoved(Level world) {
        if (!world.isClientSide) {
            TechManager.server().removeProgressChangeListener(onTechChange);
        }
    }

    public void onMachineConfig() {
        updateTargetRecipe();
        setUpdateRecipe();
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, this::setUpdateRecipe);
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, this::onMachineConfig);
    }

    @Nonnull
    @Override
    public <T1> LazyOptional<T1> getCapability(Capability<T1> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (currentRecipe != null) {
            tag.putString("currentRecipe", currentRecipe.getId().toString());
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

    private static final String ID = "machine/recipe_processor";

    public static Voltage getBlockVoltage(BlockEntity be) {
        return be.getBlockState().getBlock() instanceof MachineBlock<?> machineBlock ?
                machineBlock.voltage : Voltage.PRIMITIVE;
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    machine(RecipeTypeEntry<? extends ProcessingRecipe, ?> type) {
        return CapabilityProviderBuilder.fromFactory(ID,
                be -> new MachineProcessor<>(be, type.get(), getBlockVoltage(be)));
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P> oreProcessor(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID,
                be -> new OreAnalyzerProcessor(be, getBlockVoltage(be)));
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    generator(RecipeTypeEntry<? extends GeneratorRecipe, ?> type) {
        return CapabilityProviderBuilder.fromFactory(ID,
                be -> new GeneratorProcessor(be, type.get(), getBlockVoltage(be)));
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P>
    electricFurnace(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID,
                be -> new ElectricFurnace(be, getBlockVoltage(be)));
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    multiBlock(RecipeTypeEntry<? extends ProcessingRecipe, ?> type) {
        return CapabilityProviderBuilder.fromFactory(ID,
                be -> new MultiBlockProcessor<>(be, type.get()));
    }
}
