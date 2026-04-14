package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends CoilMachine<BlastFurnaceRecipe> {
    public BlastFurnace(IRecipeType<BlastFurnaceRecipe.Builder> recipeType,
        IRecipeManager recipeManager, IRecipeType<MarkerRecipe.Builder> markerType) {
        super(recipeType, recipeManager, markerType);
    }

    @Override
    protected int getRecipeTemperature(BlastFurnaceRecipe recipe) {
        return recipe.temperature;
    }
}
