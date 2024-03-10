package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
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
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProcessor<T extends ProcessingRecipe<?>>
        implements ICapabilityProvider, IProcessor, IElectricMachine, INBTSerializable<CompoundTag> {
    protected static final long PROGRESS_PER_TICK = 256;

    protected final BlockEntity blockEntity;
    protected final RecipeType<? extends T> recipeType;
    protected final Voltage voltage;
    protected long workProgress = 0;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    protected ResourceLocation currentRecipeId = null;
    @Nullable
    protected T currentRecipe = null;
    @Nullable
    protected IContainer container = null;
    protected boolean needUpdate = true;

    public RecipeProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType, Voltage voltage) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
        this.voltage = voltage;
    }

    protected IContainer getContainer() {
        if (this.container == null) {
            this.container = AllCapabilities.CONTAINER.getCapability(this.blockEntity);
        }
        return this.container;
    }

    protected void updateRecipe() {
        if (this.currentRecipe != null || !this.needUpdate) {
            return;
        }
        var world = this.blockEntity.getLevel();
        if (world == null) {
            return;
        }
        this.currentRecipe = SmartRecipe.getRecipeFor(this.recipeType, this.getContainer(), world)
                .orElse(null);
        this.workProgress = 0;
        if (this.currentRecipe != null) {
            this.currentRecipe.consumeInputs(this.getContainer());
            this.blockEntity.setChanged();
        }
        this.needUpdate = false;
    }

    @SuppressWarnings("unchecked")
    protected void initializeRecipe() {
        if (this.currentRecipeId == null) {
            return;
        }
        var world = this.blockEntity.getLevel();
        assert world != null;
        world.getRecipeManager().byKey(this.currentRecipeId).ifPresent(recipe -> {
            if (this.recipeType == recipe.getType()) {
                this.currentRecipe = (T) recipe;
            }
        });
        this.currentRecipeId = null;
        if (this.currentRecipe != null) {
            this.needUpdate = false;
        }
    }

    @Override
    public void onInputUpdate() {
        if (this.currentRecipe == null) {
            this.needUpdate = true;
        }
    }

    @Override
    public void onOutputUpdate() {
        this.onInputUpdate();
    }

    protected long getMaxWorkTicks() {
        if (this.currentRecipe == null) {
            return 0;
        }
        return this.currentRecipe.workTicks * PROGRESS_PER_TICK;
    }

    @Override
    public void onPreWork() {
        this.initializeRecipe();
        this.updateRecipe();
    }

    @Override
    public void onWorkTick(double partial) {
        if (this.currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * (double) PROGRESS_PER_TICK);
        this.workProgress += progress;
        var world = this.blockEntity.getLevel();
        assert world != null;
        if (this.workProgress >= this.getMaxWorkTicks()) {
            this.currentRecipe.insertOutputs(this.getContainer(), world.random);
            this.currentRecipe = null;
            this.needUpdate = true;
        }
        this.blockEntity.setChanged();
    }

    @Override
    public Optional<ProcessingRecipe<?>> getCurrentRecipe() {
        return Optional.ofNullable(this.currentRecipe);
    }

    @Override
    public double getProgress() {
        if (this.currentRecipe == null) {
            return 0;
        }
        return (double) this.workProgress / (double) this.getMaxWorkTicks();
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
        this.initializeRecipe();
        var tag = new CompoundTag();
        if (this.currentRecipe != null) {
            tag.putString("currentRecipe", this.currentRecipe.getId().toString());
            tag.putLong("workProgress", this.workProgress);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.currentRecipe = null;
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            this.currentRecipeId = new ResourceLocation(tag.getString("currentRecipe"));
            this.workProgress = tag.getLong("workProgress");
        } else {
            this.currentRecipeId = null;
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
