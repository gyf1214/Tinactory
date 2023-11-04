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
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.NullContainer;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ProcessingContainer implements ICapabilityProvider, IProcessingMachine,
        NullContainer, INBTSerializable<CompoundTag> {
    protected static final long PROGRESS_PER_TICK = 100;

    protected final BlockEntity blockEntity;
    protected final RecipeType<? extends ProcessingRecipe<?>> recipeType;
    protected long workProgress = 0;

    /**
     * This is only used during deserializeNBT when world is not available.
     */
    @Nullable
    protected ResourceLocation currentRecipeId = null;
    @Nullable
    protected ProcessingRecipe<?> currentRecipe = null;
    protected boolean needUpdate = true;

    protected ProcessingContainer(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe<?>> recipeType) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
    }

    @Override
    public abstract IItemCollection getPort(int port, boolean internal);

    protected void updateRecipe() {
        if (this.currentRecipe != null || !this.needUpdate) {
            return;
        }
        var world = this.blockEntity.getLevel();
        if (world == null) {
            return;
        }
        this.currentRecipe = world.getRecipeManager().getRecipeFor(this.recipeType, this, world).orElse(null);
        this.workProgress = 0;
        if (this.currentRecipe != null) {
            this.currentRecipe.consumeInputs(this);
        }
        this.needUpdate = false;
    }

    protected void initializeRecipe() {
        if (this.currentRecipeId == null) {
            return;
        }
        var world = this.blockEntity.getLevel();
        assert world != null;
        var recipe = world.getRecipeManager().byKey(this.currentRecipeId);
        if (recipe.isPresent() && recipe.get() instanceof ProcessingRecipe<?> processingRecipe) {
            this.currentRecipe = processingRecipe;
        }
        this.currentRecipeId = null;
        if (this.currentRecipe != null) {
            this.needUpdate = false;
        }
    }

    protected void onInputUpdate() {
        if (this.currentRecipe == null) {
            this.needUpdate = true;
        }
    }

    @Override
    public void onWorkTick(double partial) {
        this.initializeRecipe();
        this.updateRecipe();
        if (this.currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * (double) PROGRESS_PER_TICK);
        this.workProgress += progress;
        if (this.workProgress >= this.currentRecipe.workTicks * PROGRESS_PER_TICK) {
            this.currentRecipe.insertOutputs(this);
            this.currentRecipe = null;
            this.needUpdate = true;
        }
    }

    @Override
    public double getProgress() {
        if (this.currentRecipe == null) {
            return 0;
        }
        var maxProgress = this.currentRecipe.workTicks * PROGRESS_PER_TICK;
        return (double) this.workProgress / (double) maxProgress;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSING_MACHINE.get()) {
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
}
