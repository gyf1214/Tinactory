package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.machine.RecipeProcessors.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Generator extends ProcessingMachine<ProcessingRecipe> {
    public Generator(IRecipeType<ProcessingRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    protected void calculateFactors(ProcessingRecipe recipe, IMachine machine) {
        workFactor = 1d;
        energyFactor = 1d;
    }

    @Override
    public long onWorkProgress(ProcessingRecipe recipe, double partial) {
        return PROGRESS_PER_TICK;
    }

    @Override
    public ElectricMachineType electricMachineType(ProcessingRecipe recipe) {
        return ElectricMachineType.GENERATOR;
    }

    @Override
    public double powerGen(ProcessingRecipe recipe) {
        return recipe.power;
    }

    @Override
    public double powerCons(ProcessingRecipe recipe) {
        return 0;
    }
}
