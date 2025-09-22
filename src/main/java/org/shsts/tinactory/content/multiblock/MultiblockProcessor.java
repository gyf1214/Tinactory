package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockProcessor<R extends ProcessingRecipe> extends MachineProcessor<R> {
    public MultiblockProcessor(BlockEntity blockEntity,
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType, boolean autoRecipe) {
        super(blockEntity, recipeType, Voltage.PRIMITIVE, autoRecipe);
    }

    protected Multiblock getMultiblock() {
        return Multiblock.get(blockEntity);
    }

    @Override
    public long getVoltage() {
        return getMultiblock().getInterface().map($ -> $.voltage.value).orElse(0L);
    }

    @Override
    protected Optional<IMachine> getMachine() {
        return getMultiblock().getInterface().map($ -> $);
    }
}
