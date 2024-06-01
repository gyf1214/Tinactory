package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorProcessor extends RecipeProcessor<ProcessingRecipe> {
    protected GeneratorProcessor(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe> recipeType,
                                 Voltage voltage) {
        super(blockEntity, recipeType, voltage);
    }

    @Override
    protected void calculateFactors(ProcessingRecipe recipe) {
        workFactor = 1d;
        energyFactor = 1d;
    }

    @Override
    protected long getProgressPerTick(double partial) {
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
