package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceProcessor extends CoilProcessor<BlastFurnaceRecipe> {
    public BlastFurnaceProcessor(BlockEntity blockEntity,
        IRecipeType<BlastFurnaceRecipe.Builder> recipeType) {
        super(blockEntity, recipeType, true);
    }

    @Override
    protected int getRecipeTemperature(BlastFurnaceRecipe recipe) {
        return recipe.temperature;
    }
}
