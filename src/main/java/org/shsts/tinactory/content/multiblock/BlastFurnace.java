package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends CoilMachine<BlastFurnaceRecipe> {
    public BlastFurnace(IRecipeType<BlastFurnaceRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    protected int getRecipeTemperature(BlastFurnaceRecipe recipe) {
        return recipe.temperature;
    }
}
