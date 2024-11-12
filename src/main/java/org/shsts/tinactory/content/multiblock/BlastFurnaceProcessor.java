package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceProcessor extends MultiBlockProcessor<BlastFurnaceRecipe> {
    public BlastFurnaceProcessor(BlockEntity blockEntity) {
        super(blockEntity, AllRecipes.BLAST_FURNACE.get());
    }

    @Override
    protected BlastFurnace getMultiBlock() {
        return (BlastFurnace) super.getMultiBlock();
    }

    private int getTemperature() {
        return getMultiBlock().getTemperature().orElse(0);
    }

    @Override
    protected boolean matchesRecipe(BlastFurnaceRecipe recipe) {
        return super.matchesRecipe(recipe) && recipe.temperature <= getTemperature();
    }

    @Override
    protected void calculateFactors(BlastFurnaceRecipe recipe) {
        super.calculateFactors(recipe);
        var temp = getTemperature();
        var factor = Math.max(1d, (temp - recipe.temperature) /
            TinactoryConfig.INSTANCE.blastFurnaceTempFactor.get());
        energyFactor /= factor;
    }
}
