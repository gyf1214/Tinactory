package org.shsts.tinactory.content.gui;

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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerMenu extends Menu<NetworkController> {
    private final int syncSlot;

    public NetworkControllerMenu(SmartMenuType<NetworkController, ?> type, int id,
                                 Inventory inventory, NetworkController blockEntity) {
        super(type, id, inventory, blockEntity);

        this.syncSlot = addSyncSlot(NetworkControllerSyncPacket.class, NetworkControllerSyncPacket::new);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.canPlayerInteract(player) && super.stillValid(player);
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    @Override
    public <M1 extends Menu<?>> MenuScreen<M1> createScreen(Inventory inventory, Component title) {
        return (MenuScreen<M1>) new NetworkControllerScreen(this, inventory, title, syncSlot);
    }
}
