package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllBlockEntityEvents;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProcessor<T extends ProcessingRecipe<?>> implements ICapabilityProvider,
        IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final long PROGRESS_PER_TICK = 256;

    private final BlockEntity blockEntity;
    private final RecipeType<? extends T> recipeType;
    private final Voltage voltage;
    private long workProgress = 0;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    @Nullable
    private T currentRecipe = null;
    @Nullable
    private ResourceLocation targetRecipeLoc = null;
    @Nullable
    private T targetRecipe = null;
    @Nullable
    private IContainer container = null;
    private boolean needUpdate = true;

    public RecipeProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType, Voltage voltage) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
        this.voltage = voltage;
    }

    private IContainer getContainer() {
        if (this.container == null) {
            this.container = AllCapabilities.CONTAINER.getCapability(this.blockEntity);
        }
        return this.container;
    }

    private Level getWorld() {
        var world = this.blockEntity.getLevel();
        assert world != null;
        return world;
    }

    private void updateRecipe() {
        if (this.currentRecipe != null || !this.needUpdate) {
            return;
        }
        var world = this.getWorld();
        assert this.currentRecipeLoc == null;
        this.currentRecipe = null;
        if (this.targetRecipe != null) {
            if (this.targetRecipe.matches(this.getContainer(), world)) {
                this.currentRecipe = this.targetRecipe;
            }
        } else {
            var matches = SmartRecipe.getRecipesFor(this.recipeType, this.getContainer(), world);
            if (matches.size() == 1) {
                this.currentRecipe = matches.get(0);
            }
        }
        this.workProgress = 0;
        if (this.currentRecipe != null) {
            this.currentRecipe.consumeInputs(this.getContainer());
        }
        this.needUpdate = false;
        this.blockEntity.setChanged();
    }

    private long getMaxWorkTicks() {
        assert this.currentRecipe != null;
        return this.currentRecipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public void onPreWork() {
        this.updateRecipe();
    }

    @Override
    public void onWorkTick(double partial) {
        if (this.currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * (double) PROGRESS_PER_TICK);
        this.workProgress += progress;
        var world = this.getWorld();
        if (this.workProgress >= this.getMaxWorkTicks()) {
            assert this.currentRecipe != null;
            this.currentRecipe.insertOutputs(this.getContainer(), world.random);
            this.currentRecipe = null;
            this.needUpdate = true;
        }
        this.blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        if (this.currentRecipe == null) {
            return 0;
        }
        return (double) this.workProgress / (double) this.getMaxWorkTicks();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTargetRecipe(@Nullable ProcessingRecipe<?> recipe) {
        assert this.targetRecipeLoc == null;
        if (recipe == null) {
            this.targetRecipe = null;
        } else if (recipe.getType() == this.recipeType) {
            this.targetRecipe = (T) recipe;
        }
        this.blockEntity.setChanged();
    }

    @Override
    public Optional<ProcessingRecipe<?>> getTargetRecipe() {
        return Optional.ofNullable(this.targetRecipe);
    }

    @Override
    public long getVoltage() {
        return this.voltage.val;
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
        return this.voltage == Voltage.PRIMITIVE || this.currentRecipe == null ?
                0 : this.currentRecipe.power;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private T recipeByKey(RecipeManager recipeManager, @Nullable ResourceLocation loc) {
        return (T) Optional.ofNullable(loc)
                .flatMap(recipeManager::byKey)
                .filter(r -> r.getType() == this.recipeType)
                .orElse(null);
    }

    private void onLoad(Level world) {
        var recipeManager = world.getRecipeManager();

        this.currentRecipe = this.recipeByKey(recipeManager, this.currentRecipeLoc);
        this.currentRecipeLoc = null;
        if (this.currentRecipe != null) {
            this.needUpdate = false;
        }

        this.targetRecipe = this.recipeByKey(recipeManager, this.targetRecipeLoc);
        this.targetRecipeLoc = null;
    }

    private void onContainerChange(boolean isInput) {
        if (this.currentRecipe == null) {
            this.needUpdate = true;
        }
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllBlockEntityEvents.SERVER_LOAD, this::onLoad);
        eventManager.subscribe(AllBlockEntityEvents.CONTAINER_CHANGE, this::onContainerChange);
    }

    @NotNull
    @Override
    public <T1> LazyOptional<T1> getCapability(Capability<T1> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return LazyOptional.of(() -> this).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var recipeLoc = this.currentRecipeLoc != null ? this.currentRecipeLoc :
                (this.currentRecipe != null ? this.currentRecipe.getId() : null);
        if (recipeLoc != null) {
            tag.putString("currentRecipe", recipeLoc.toString());
            tag.putLong("workProgress", this.workProgress);
        }
        var targetRecipeLoc = this.targetRecipeLoc != null ? this.targetRecipeLoc :
                (this.targetRecipe != null ? this.targetRecipe.getId() : null);
        if (targetRecipeLoc != null) {
            tag.putString("targetRecipe", targetRecipeLoc.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.currentRecipe = null;
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            this.currentRecipeLoc = new ResourceLocation(tag.getString("currentRecipe"));
            this.workProgress = tag.getLong("workProgress");
        } else {
            this.currentRecipeLoc = null;
        }
        if (tag.contains("targetRecipe", Tag.TAG_STRING)) {
            this.targetRecipeLoc = new ResourceLocation(tag.getString("targetRecipe"));
        } else {
            this.targetRecipeLoc = null;
        }
    }

    public static class Builder implements Function<BlockEntity, ICapabilityProvider> {
        @Nullable
        private RecipeType<? extends ProcessingRecipe<?>> recipeType = null;
        @Nullable
        private Voltage voltage = null;

        public Builder recipeType(RecipeType<? extends ProcessingRecipe<?>> recipeType) {
            this.recipeType = recipeType;
            return this;
        }

        public Builder voltage(Voltage voltage) {
            this.voltage = voltage;
            return this;
        }

        @Override
        public ICapabilityProvider apply(BlockEntity blockEntity) {
            assert this.recipeType != null;
            assert this.voltage != null;
            return new RecipeProcessor<>(blockEntity, this.recipeType, voltage);
        }
    }
}
