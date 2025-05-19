package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CoilProcessor<R extends ProcessingRecipe> extends MultiblockProcessor<R> {
    public CoilProcessor(BlockEntity blockEntity,
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType,
        boolean autoRecipe) {
        super(blockEntity, recipeType, autoRecipe);
    }

    @Override
    protected CoilMultiblock getMultiblock() {
        return (CoilMultiblock) super.getMultiblock();
    }

    private int getTemperature() {
        return getMultiblock().getTemperature().orElse(0);
    }

    protected abstract int getRecipeTemperature(R recipe);

    @Override
    protected void calculateFactors(R recipe) {
        super.calculateFactors(recipe);
        var temp = getTemperature();
        var factor = Math.max(1d, (temp - getRecipeTemperature(recipe)) /
            CONFIG.blastFurnaceTempFactor.get());
        energyFactor /= factor;
    }
}
