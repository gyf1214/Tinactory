package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends CoilMachine<BlastFurnaceRecipe> {
    public BlastFurnace(IRecipeType<BlastFurnaceRecipe> recipeType,
        Supplier<IRecipeManager> recipeManager, IRecipeType<MarkerRecipe> markerType) {
        super(recipeType, recipeManager, markerType);
    }

    @Override
    protected int getRecipeTemperature(BlastFurnaceRecipe recipe) {
        return recipe.temperature;
    }
}
