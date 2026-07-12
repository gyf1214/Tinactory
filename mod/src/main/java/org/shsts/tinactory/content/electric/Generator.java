package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Generator extends ProcessingMachine<ProcessingRecipe> {
    public Generator(IRecipeType<ProcessingRecipe> recipeType,
        Supplier<IRecipeManager> recipeManager, IRecipeType<MarkerRecipe> markerType) {
        super(recipeType, recipeManager, markerType);
    }

    @Override
    protected int calculateParallel(ProcessingRecipe recipe, IMachine machine, int maxParallel) {
        var voltage = machineVoltage(machine);
        var recipeParallel = (int) (voltage / recipe.voltage);
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (recipe.matches(machine, m * recipeParallel)) {
                l = m;
            } else {
                r = m;
            }
        }
        return recipeParallel * l;
    }

    @Override
    protected void calculateFactors(ProcessingRecipe recipe, IMachine machine, int parallel) {
        workFactor = 1d;
        energyFactor = parallel;
    }

    @Override
    public long onWorkProgress(IEntry<ProcessingRecipe> recipe, double partial) {
        return PROGRESS_PER_TICK;
    }

    @Override
    public long workTicksFromProgress(long progress) {
        return progress / PROGRESS_PER_TICK;
    }

    @Override
    public double workSpeed(double partial) {
        return 1d;
    }

    @Override
    public ElectricMachineType electricMachineType(IEntry<ProcessingRecipe> recipe) {
        return ElectricMachineType.GENERATOR;
    }

    @Override
    public double powerGen(IEntry<ProcessingRecipe> recipe) {
        return energyFactor * recipe.get().power;
    }

    @Override
    public double powerCons(IEntry<ProcessingRecipe> recipe) {
        return 0;
    }
}
