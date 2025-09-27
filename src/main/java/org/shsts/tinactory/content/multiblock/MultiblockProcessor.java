package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.multiblock.Multiblock;

import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockProcessor extends MachineProcessor {
    public MultiblockProcessor(BlockEntity blockEntity,
        Collection<? extends IRecipeProcessor<?>> processors, boolean autoRecipe) {
        super(blockEntity, processors, autoRecipe);
    }

    @Override
    protected Optional<IMachine> machine() {
        return Multiblock.get(blockEntity).getInterface().map($ -> $);
    }
}
