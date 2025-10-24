package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.gui.InventoryMenu;
import org.shsts.tinactory.core.gui.Menu;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.ProcessingMenu.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends InventoryMenu {
    public static final int CONFIG_WIDTH = BUTTON_SIZE * 4 + 2;
    public static final String SLOT_SYNC = "info";

    public final IMachine machine;
    private final LogisticComponent logistic;
    private final BlockPos subnet;
    private final Runnable onUpdatePorts;

    public LogisticWorkerMenu(Properties properties) {
        super(properties, MARGIN_X + CONFIG_WIDTH, Menu.PANEL_HEIGHT);

        var scheduler = new ActiveScheduler<>(() ->
            new LogisticWorkerSyncPacket(getVisiblePorts()));
        this.onUpdatePorts = scheduler::invokeUpdate;
        addSyncSlot(SLOT_SYNC, scheduler);

        this.machine = MACHINE.get(blockEntity);
        if (!world.isClientSide) {
            var network = machine.network().orElseThrow();
            this.logistic = network.getComponent(LOGISTIC_COMPONENT.get());
            this.subnet = network.getSubnet(blockEntity.getBlockPos());
            logistic.onUpdate(onUpdatePorts);
        } else {
            this.logistic = null;
            this.subnet = null;
        }

        onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(blockEntity).setConfig(p));
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!world.isClientSide) {
            logistic.unregisterCallback(onUpdatePorts);
        }
    }

    private List<LogisticWorkerSyncPacket.PortInfo> getVisiblePorts() {
        var ret = new ArrayList<LogisticWorkerSyncPacket.PortInfo>();
        for (var info : logistic.getVisiblePorts(subnet)) {
            var machine1 = info.machine();
            var index = info.portIndex();
            var portName = portLabel(info.port().type(), index);

            ret.add(new LogisticWorkerSyncPacket.PortInfo(machine1.uuid(),
                index, machine1.title(), machine1.icon(), portName));
        }
        return ret;
    }
}
