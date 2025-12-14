package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Generator extends ProcessingMachine<ProcessingRecipe> {
    public Generator(IRecipeType<ProcessingRecipe.Builder> recipeType) {
        super(recipeType);
    }

    @Override
    protected int calculateParallel(ProcessingRecipe recipe, Level world, IMachine machine, int maxParallel) {
        var voltage = machineVoltage(machine);
        var recipeParallel = (int) (voltage / recipe.voltage);
        var l = 1;
        var r = maxParallel + 1;
        while (r - l > 1) {
            var m = (l + r) / 2;
            if (recipe.matches(machine, world, m * recipeParallel)) {
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
    public long onWorkProgress(ProcessingRecipe recipe, double partial) {
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
    public ElectricMachineType electricMachineType(ProcessingRecipe recipe) {
        return ElectricMachineType.GENERATOR;
    }

    @Override
    public double powerGen(ProcessingRecipe recipe) {
        return energyFactor * recipe.power;
    }

    @Override
    public double powerCons(ProcessingRecipe recipe) {
        return 0;
    }
}
