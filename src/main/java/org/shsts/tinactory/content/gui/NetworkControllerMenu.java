package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.content.gui.sync.SetNetworkControllerPacket;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.tech.TeamProfile;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerMenu extends Menu<NetworkController> {
    public NetworkControllerMenu(SmartMenuType<NetworkController, ?> type, int id,
                                 Inventory inventory, NetworkController blockEntity) {
        super(type, id, inventory, blockEntity);
        onEventPacket(MenuEventHandler.SET_NETWORK_CONTROLLER, this::onSetNetworkController);
    }

    @Override
    public boolean stillValid(Player player) {
        if (!super.stillValid(player)) {
            return false;
        }
        var teamName = blockEntity.getOwnerTeam().map(TeamProfile::getName).orElse(null);
        var curTeamName = player.getTeam() == null ? null : player.getTeam().getName();
        return Objects.equals(teamName, curTeamName);
    }

    private void onSetNetworkController(SetNetworkControllerPacket packet) {
        blockEntity.initByPlayer(player);
    }
}
