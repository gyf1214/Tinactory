package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorProcessor extends MachineProcessor<ProcessingRecipe> {
    public GeneratorProcessor(BlockEntity blockEntity,
        IRecipeType<ProcessingRecipe.Builder> recipeType) {
        super(blockEntity, recipeType, true);
    }

    @Override
    protected void calculateFactors(ProcessingRecipe recipe) {
        workFactor = 1d;
        energyFactor = 1d;
    }

    @Override
    protected long onWorkProgress(ProcessingRecipe recipe, double partial) {
        return PROGRESS_PER_TICK;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.GENERATOR;
    }

    @Override
    public double getPowerGen() {
        return currentRecipe == null ? 0 : currentRecipe.power;
    }

    @Override
    public double getPowerCons() {
        return 0d;
    }
}
