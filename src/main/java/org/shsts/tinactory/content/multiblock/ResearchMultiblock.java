package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchMultiblock extends Multiblock {
    public ResearchMultiblock(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    public IMenuType menu(IMachine machine) {
        return AllMenus.RESEARCH_BENCH;
    }
}
