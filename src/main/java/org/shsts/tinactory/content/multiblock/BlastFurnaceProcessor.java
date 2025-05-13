package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceProcessor extends CoilProcessor<BlastFurnaceRecipe> {
    public BlastFurnaceProcessor(BlockEntity blockEntity) {
        super(blockEntity, AllRecipes.BLAST_FURNACE, true);
    }

    @Override
    protected int getRecipeTemperature(BlastFurnaceRecipe recipe) {
        return recipe.temperature;
    }
}
