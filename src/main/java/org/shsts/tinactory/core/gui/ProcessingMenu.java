package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.machine.Machine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMenu<T extends BlockEntity> extends Menu<T> {
    public ProcessingMenu(SmartMenuType<T, ?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id, inventory, blockEntity);
    }

    @Override
    public boolean stillValid(Player player) {
        return Machine.get(blockEntity).canPlayerInteract(player) && super.stillValid(player);
    }
}
