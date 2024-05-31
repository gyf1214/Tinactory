package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProcessor<T extends ProcessingRecipe> extends CapabilityProvider
        implements IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long PROGRESS_PER_TICK = 256;

    private final BlockEntity blockEntity;
    protected final RecipeType<? extends T> recipeType;
    protected final Voltage voltage;

    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;

    private long workProgress = 0;
    private double workFactor = 1d;
    private double energyFactor = 1d;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    @Nullable
    private T currentRecipe = null;
    @Nullable
    private IContainer container = null;
    private boolean needUpdate = true;

    protected RecipeProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType, Voltage voltage) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
        this.voltage = voltage;
    }

    protected IContainer getContainer() {
        if (container == null) {
            container = AllCapabilities.CONTAINER.get(blockEntity);
        }
        return container;
    }

    private Level getWorld() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private T getTargetRecipe() {
        var world = blockEntity.getLevel();
        assert world != null && !world.isClientSide;
        return (T) Machine.tryGet(blockEntity)
                .flatMap(m -> m.config.getRecipe("targetRecipe", world))
                .filter(r -> r.getType() == recipeType)
                .orElse(null);
    }

    private void calculateFactors(ProcessingRecipe recipe) {
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

    protected Optional<T> getNewRecipe(Level world, IContainer container) {
        var targetRecipe = getTargetRecipe();
        if (targetRecipe != null) {
            if (targetRecipe.matches(container, world) && targetRecipe.voltage <= voltage.value) {
                return Optional.of(targetRecipe);
            }
        } else {
            var matches = SmartRecipe.getRecipesFor(recipeType, container, world)
                    .stream().filter(r -> r.voltage <= voltage.value)
                    .toList();
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
        var container = getContainer();
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
        if (team == getContainer().getOwnerTeam().orElse(null)) {
            LOGGER.debug("processor {}: on tech change", this);
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
        recipe.insertOutputs(getContainer(), random);
    }

    @Override
    public void onWorkTick(double partial) {
        if (currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
        workProgress += progress;
        if (workProgress >= getMaxWorkTicks()) {
            assert currentRecipe != null;
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

    @SuppressWarnings("unchecked")
    private void onServerLoad(Level world) {
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

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, $ -> setUpdateRecipe());
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, $ -> setUpdateRecipe());
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

    public static class Builder<P> extends CapabilityProviderBuilder<BlockEntity, P> {
        @Nullable
        private Supplier<? extends RecipeType<? extends ProcessingRecipe>> recipeType = null;
        @Nullable
        private Voltage voltage = null;

        public Builder(P parent) {
            super(parent, "machine/recipe_processor");
        }

        public Builder<P> recipeType(Supplier<? extends RecipeType<? extends ProcessingRecipe>> recipeType) {
            this.recipeType = recipeType;
            return this;
        }

        public Builder<P> voltage(Voltage voltage) {
            this.voltage = voltage;
            return this;
        }

        @Override
        protected Function<BlockEntity, ICapabilityProvider> createObject() {
            var recipeType = this.recipeType;
            var voltage = this.voltage;
            assert recipeType != null;
            assert voltage != null;
            return be -> new RecipeProcessor<>(be, recipeType.get(), voltage);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(parent);
    }
}
