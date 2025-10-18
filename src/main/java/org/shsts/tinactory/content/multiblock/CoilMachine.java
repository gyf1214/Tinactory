package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CoilMachine<R extends ProcessingRecipe> extends ProcessingMachine<R> {
    public CoilMachine(IRecipeType<? extends IRecipeBuilderBase<R>> recipeType) {
        super(recipeType);
    }

    private int getTemperature(IMachine machine) {
        return CoilMultiblock.getTemperature(machine).orElse(0);
    }

    protected abstract int getRecipeTemperature(R recipe);

    @Override
    protected void calculateFactors(R recipe, IMachine machine, int parallel) {
        super.calculateFactors(recipe, machine, parallel);
        var temp = getTemperature(machine);
        var factor = Math.max(1d, (temp - getRecipeTemperature(recipe)) /
            CONFIG.coilTemperatureFactor.get());
        energyFactor /= factor;
    }
}
