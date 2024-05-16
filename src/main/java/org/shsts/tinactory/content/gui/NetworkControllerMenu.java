package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.network.NetworkController;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerMenu extends Menu<NetworkController> {
    public NetworkControllerMenu(SmartMenuType<NetworkController, ?> type, int id,
                                 Inventory inventory, NetworkController blockEntity) {
        super(type, id, inventory, blockEntity);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.canPlayerInteract(player) && super.stillValid(player);
    }
}
