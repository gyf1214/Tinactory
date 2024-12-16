package org.shsts.tinactory.content.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

public class MultiBlockProcessor<R extends ProcessingRecipe> extends MachineProcessor<R> {
    public MultiBlockProcessor(BlockEntity blockEntity,
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType, boolean autoRecipe) {
        super(blockEntity, recipeType, Voltage.PRIMITIVE, autoRecipe);
    }

    protected MultiBlock getMultiBlock() {
        return MultiBlock.get(blockEntity);
    }

    @Override
    public long getVoltage() {
        return getMultiBlock().getInterface().map($ -> $.voltage.value).orElse(0L);
    }
}
