package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.network.NetworkController;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerMenu extends Menu<NetworkController, NetworkControllerMenu> {
    private final int syncSlot;

    public NetworkControllerMenu(SmartMenuType<NetworkController, ?> type, int id,
        Inventory inventory, NetworkController blockEntity) {
        super(type, id, inventory, blockEntity);
        this.syncSlot = addSyncSlot(NetworkControllerSyncPacket::new);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.canPlayerInteract(player) && super.stillValid(player);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen<NetworkControllerMenu> createScreen(Inventory inventory, Component title) {
        return new NetworkControllerScreen(this, inventory, title, syncSlot);
    }
}
