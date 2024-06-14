package org.shsts.tinactory.content.multiblock;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

public class MultiBlockProcessor<T extends ProcessingRecipe> extends MachineProcessor<T> {
    public MultiBlockProcessor(BlockEntity blockEntity, RecipeType<? extends T> recipeType) {
        super(blockEntity, recipeType, Voltage.PRIMITIVE);
    }

    protected MultiBlock getMultiBlock() {
        return AllCapabilities.MULTI_BLOCK.get(blockEntity);
    }

    @Override
    public long getVoltage() {
        return getMultiBlock().getInterface().map($ -> $.voltage.value).orElse(0L);
    }
}
