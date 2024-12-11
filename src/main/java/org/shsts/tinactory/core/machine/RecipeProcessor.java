package org.shsts.tinactory.core.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.shsts.tinactory.content.electric.GeneratorProcessor;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.ElectricFurnace;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.content.machine.OreAnalyzerProcessor;
import org.shsts.tinactory.content.multiblock.BlastFurnaceProcessor;
import org.shsts.tinactory.content.multiblock.MultiBlockProcessor;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeProcessor<T extends Recipe<?>> extends CapabilityProvider implements
    IProcessor, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String ID = "machine/recipe_processor";
    public static final long PROGRESS_PER_TICK = 256;

    protected final BlockEntity blockEntity;
    public final RecipeType<? extends T> recipeType;
    private final boolean autoRecipe;

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

    protected RecipeProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType,
        boolean autoRecipe) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
        this.autoRecipe = autoRecipe;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> machine(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> type) {
        return $ -> $.capability(ID, be ->
            new MachineProcessor<>(be, type.get(), true));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> noAutoRecipe(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> type) {
        return $ -> $.capability(ID, be ->
            new MachineProcessor<>(be, type.get(), false));
    }

    public static <P> IBlockEntityTypeBuilder<P> oreProcessor(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, OreAnalyzerProcessor::new);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> generator(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> type) {
        return $ -> $.capability(ID, be ->
            new GeneratorProcessor(be, type.get()));
    }

    public static <P> IBlockEntityTypeBuilder<P> electricFurnace(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, ElectricFurnace::new);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> multiBlock(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> type, boolean autoRecipe) {
        return $ -> $.capability(ID, be ->
            new MultiBlockProcessor<>(be, type.get(), autoRecipe));
    }

    public static <P> IBlockEntityTypeBuilder<P> blastFurnace(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, BlastFurnaceProcessor::new);
    }

    protected Level getWorld() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    protected abstract boolean matches(Level world, T recipe, IContainer container);

    protected abstract Stream<? extends T> getMatchedRecipes(Level world, IContainer container);

    protected Optional<T> getNewRecipe(Level world, IContainer container) {
        if (targetRecipe != null) {
            if (matches(world, targetRecipe, container)) {
                return Optional.of(targetRecipe);
            }
        } else if (autoRecipe) {
            var matches = getMatchedRecipes(world, container).toList();
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

    public abstract boolean allowTargetRecipe(Recipe<?> recipe);

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
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), this::onServerLoad);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(CONTAINER_CHANGE.get(), this::setUpdateRecipe);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
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

    public static Voltage getBlockVoltage(BlockEntity be) {
        return be.getBlockState().getBlock() instanceof MachineBlock machineBlock ?
            machineBlock.voltage : Voltage.PRIMITIVE;
    }
}
