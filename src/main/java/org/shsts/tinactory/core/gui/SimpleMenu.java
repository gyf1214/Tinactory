package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SimpleMenu extends Menu<BlockEntity, SimpleMenu> {
    public SimpleMenu(SmartMenuType<? extends BlockEntity, ?> type, int id,
        Inventory inventory, BlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);
    }
}
