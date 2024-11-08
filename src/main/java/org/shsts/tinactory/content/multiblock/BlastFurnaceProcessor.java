package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

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
        return getMultiBlock().getTemperature();
    }

    @Override
    protected boolean matchesExtra(Level world, BlastFurnaceRecipe recipe, IContainer container) {
        return super.matchesExtra(world, recipe, container) && recipe.temperature <= getTemperature();
    }

    @Override
    protected void calculateFactors(BlastFurnaceRecipe recipe) {
        super.calculateFactors(recipe);
        var temp = getTemperature();
        if (recipe.temperature < temp) {
            energyFactor /= Math.exp(TinactoryConfig.INSTANCE.blastFurnaceTempFactor.get() *
                    (temp - recipe.temperature));
        }
    }
}
