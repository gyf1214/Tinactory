package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockProcessor extends MachineProcessor {
    public MultiblockProcessor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe) {
        super(blockEntity, processorFactories, autoRecipe);
    }

    private Optional<MultiblockInterface> getInterface() {
        return Multiblock.get(blockEntity).getInterface();
    }

    @Override
    protected Optional<IMachine> machine() {
        return getInterface().map($ -> $);
    }

    @Override
    protected int maxParallel() {
        return getInterface().map(MultiblockInterface::parallel).orElse(1);
    }
}
