package org.shsts.tinactory.content.machine;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
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
import java.util.function.Predicate;

import static org.shsts.tinactory.content.AllRecipes.MARKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor<T extends ProcessingRecipe> extends CapabilityProvider
        implements IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final long PROGRESS_PER_TICK = 256;

    protected final BlockEntity blockEntity;
    public final RecipeType<? extends T> recipeType;
    protected final Voltage voltage;

    protected long workProgress = 0;
    protected double workFactor = 1d;
    protected double energyFactor = 1d;

    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    @Nullable
    protected T currentRecipe = null;
    @Nullable
    protected T targetRecipe = null;
    protected IContainer container;
    private boolean needUpdate = true;

    protected MachineProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType, Voltage voltage) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
        this.voltage = voltage;
    }

    protected Level getWorld() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    @SuppressWarnings("unchecked")
    protected void setTargetRecipe(ResourceLocation loc) {
        var world = blockEntity.getLevel();
        assert world != null;

        var recipe = ProcessingRecipe.byKey(world.getRecipeManager(), loc).orElse(null);
        if (recipe == null || (recipe.getType() != recipeType && (recipe.getType() != MARKER.get() ||
                ((MarkerRecipe) recipe).baseType != recipeType))) {
            resetTargetRecipe();
            return;
        }

        if (recipe.getType() == MARKER.get()) {
            targetRecipe = null;
        } else {
            targetRecipe = (T) recipe;
        }

        var itemFilters = ArrayListMultimap.<Integer, Predicate<ItemStack>>create();
        var fluidFilters = ArrayListMultimap.<Integer, Predicate<FluidStack>>create();

        for (var input : recipe.inputs) {
            var idx = input.port();
            var ingredient = input.ingredient();
            if (!container.hasPort(idx)) {
                continue;
            }
            if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase item) {
                itemFilters.put(idx, item.ingredient);
            } else if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
                var stack1 = item.stack();
                itemFilters.put(idx, stack -> ItemHelper.canItemsStack(stack, stack1));
            } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                var stack1 = fluid.fluid();
                fluidFilters.put(idx, stack -> stack.isFluidEqual(stack1));
            }
        }

        for (var idx : itemFilters.keys().elementSet()) {
            var port = container.getPort(idx, false);
            port.asItem().setItemFilter(itemFilters.get(idx));
        }

        for (var idx : fluidFilters.keys().elementSet()) {
            var port = container.getPort(idx, false);
            port.asFluid().setFluidFilter(fluidFilters.get(idx));
        }
    }

    protected void resetTargetRecipe() {
        targetRecipe = null;
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
    }

    private void updateTargetRecipe() {
        var loc = AllCapabilities.MACHINE.tryGet(blockEntity)
                .flatMap(m -> m.config.getLoc("targetRecipe"))
                .orElse(null);
        LOGGER.debug("update target recipe = {}", loc);
        if (loc == null) {
            resetTargetRecipe();
        } else {
            setTargetRecipe(loc);
        }
    }

    protected void calculateFactors(ProcessingRecipe recipe) {
        var baseVoltage = recipe.voltage == 0 ? Voltage.ULV.value : recipe.voltage;
        var voltageFactor = 1L;
        var overclock = 1L;
        while (baseVoltage * voltageFactor * 4 <= voltage.value) {
            overclock *= 2;
            voltageFactor *= 4;
        }
        energyFactor = voltageFactor;
        workFactor = overclock;
    }

    protected List<T> getMatchedRecipes(Level world) {
        return SmartRecipe.getRecipesFor(recipeType, container, world)
                .stream().filter(r -> r.canCraftInVoltage(voltage.value))
                .map($ -> (T) $)
                .toList();
    }

    protected Optional<T> getNewRecipe(Level world, IContainer container) {
        if (targetRecipe != null) {
            if (targetRecipe.matches(container, world) && targetRecipe.canCraftInVoltage(voltage.value)) {
                return Optional.of(targetRecipe);
            }
        } else {
            var matches = getMatchedRecipes(world);
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
        currentRecipe = getNewRecipe(world, container).orElse(null);
        workProgress = 0;
        if (currentRecipe != null) {
            currentRecipe.consumeInputs(container);
            calculateFactors(currentRecipe);
        }
        needUpdate = false;
        blockEntity.setChanged();
    }

    private void onTechChange(ITeamProfile team) {
        if (team == container.getOwnerTeam().orElse(null)) {
            setUpdateRecipe();
        }
    }

    private long getMaxWorkTicks() {
        assert currentRecipe != null;
        return currentRecipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public void onPreWork() {
        updateRecipe();
    }

    protected void onWorkDone(T recipe, Random random) {
        recipe.insertOutputs(container, random);
    }

    protected long getProgressPerTick(double partial) {
        return (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
    }

    @Override
    public void onWorkTick(double partial) {
        if (currentRecipe == null) {
            return;
        }
        var progress = getProgressPerTick(partial);
        workProgress += progress;
        if (workProgress >= getMaxWorkTicks()) {
            onWorkDone(currentRecipe, getWorld().random);
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
        return (double) workProgress / (double) getMaxWorkTicks();
    }

    @Override
    public long getVoltage() {
        return voltage.value;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double getPowerGen() {
        return 0;
    }

    @Override
    public double getPowerCons() {
        return voltage == Voltage.PRIMITIVE || currentRecipe == null ?
                0 : currentRecipe.power * energyFactor;
    }

    private void onLoad() {
        container = AllCapabilities.CONTAINER.get(blockEntity);
    }

    @SuppressWarnings("unchecked")
    private void onServerLoad(Level world) {
        onLoad();

        var recipeManager = world.getRecipeManager();
        currentRecipe = (T) Optional.ofNullable(currentRecipeLoc)
                .flatMap(recipeManager::byKey)
                .filter(r -> r.getType() == recipeType)
                .orElse(null);
        currentRecipeLoc = null;
        if (currentRecipe != null) {
            calculateFactors(currentRecipe);
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

    private void setUpdateRecipe() {
        if (currentRecipe == null) {
            needUpdate = true;
        }
    }

    private void onMachineConfig() {
        updateTargetRecipe();
        setUpdateRecipe();
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, $ -> setUpdateRecipe());
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, this::onMachineConfig);
    }

    @Nonnull
    @Override
    public <T1> LazyOptional<T1> getCapability(Capability<T1> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get() ||
                (cap == AllCapabilities.ELECTRIC_MACHINE.get() && voltage != Voltage.PRIMITIVE)) {
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

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    basic(RecipeTypeEntry<? extends ProcessingRecipe, ?> type, Voltage voltage) {
        return CapabilityProviderBuilder.fromFactory(ID, be -> new MachineProcessor<>(be, type.get(), voltage));
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> oreProcessor(Voltage voltage) {
        return CapabilityProviderBuilder.fromFactory(ID, be -> new OreAnalyzerProcessor(be, voltage));
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    generator(RecipeTypeEntry<? extends GeneratorRecipe, ?> type, Voltage voltage) {
        return CapabilityProviderBuilder.fromFactory(ID, be -> new GeneratorProcessor(be, type.get(), voltage));
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    electricFurnace(Voltage voltage) {
        return CapabilityProviderBuilder.fromFactory(ID, be -> new ElectricFurnace(be, voltage));
    }
}
