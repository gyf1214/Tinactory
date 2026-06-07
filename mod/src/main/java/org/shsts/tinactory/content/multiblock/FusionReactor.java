package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactor extends Multiblock {
    public FusionReactor(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    public IMenuType menu(MultiblockInterface machine) {
        return machine.isDigital() ? AllMenus.FUSION_DIGITAL_INTERFACE : AllMenus.FUSION;
    }
}
