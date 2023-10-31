package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.NullContainer;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ProcessingContainer implements NullContainer, INBTSerializable<CompoundTag> {
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

    protected ProcessingContainer(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe<?>> recipeType) {
        this.blockEntity = blockEntity;
        this.recipeType = recipeType;
    }

    public abstract IItemCollection getPort(int port);

    protected void updateRecipe() {
        var world = this.blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        this.currentRecipe = world.getRecipeManager().getRecipeFor(this.recipeType, this, world).orElse(null);
        this.workProgress = 0;
        if (this.currentRecipe != null) {
            this.currentRecipe.consumeInputs(this);
        }
    }

    /**
     * Must be called from Server.
     */
    public void onWorkTick(double partial) {
        if (this.currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * (double) PROGRESS_PER_TICK);
        this.workProgress += progress;
        if (this.workProgress >= this.currentRecipe.workTicks * PROGRESS_PER_TICK) {
            this.currentRecipe.insertOutputs(this);
            this.updateRecipe();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (this.currentRecipe != null) {
            tag.putString("currentRecipe", this.currentRecipe.getId().toString());
            tag.putLong("workProgress", this.workProgress);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            this.currentRecipeId = new ResourceLocation(tag.getString("currentRecipe"));
            this.workProgress = tag.getLong("workProgress");
        } else {
            this.currentRecipeId = null;
        }
    }

    public void onLoad() {
        var world = this.blockEntity.getLevel();
        assert world != null;
        if (world.isClientSide) {
            return;
        }
        this.currentRecipe = null;
        if (this.currentRecipeId != null) {
            var recipe = world.getRecipeManager().byKey(this.currentRecipeId);
            if (recipe.isPresent() && recipe.get() instanceof ProcessingRecipe<?> processingRecipe) {
                this.currentRecipe = processingRecipe;
            }
        }
        if (this.currentRecipe == null) {
            this.workProgress = 0;
        }
        this.currentRecipeId = null;
    }
}
