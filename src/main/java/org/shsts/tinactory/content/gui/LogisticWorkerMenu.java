package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.gui.InventoryMenu;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.ProcessingMenu.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends InventoryMenu {
    public static final int BUTTON_SIZE = MachineRecipeBook.BUTTON_SIZE;
    public static final int CONFIG_WIDTH = BUTTON_SIZE * 4 + 2;
    public static final int PORT_WIDTH = 42;
    public static final int WIDTH = CONFIG_WIDTH + SLOT_SIZE * 9 + PORT_WIDTH + 2 * MARGIN_X;
    private static final int PANEL_HEIGHT = 128;

    private class SyncScheduler implements ISyncSlotScheduler<LogisticWorkerSyncPacket> {
        @Override
        public boolean shouldSend() {
            return needUpdate;
        }

        @Override
        public LogisticWorkerSyncPacket createPacket() {
            needUpdate = false;
            return new LogisticWorkerSyncPacket(getVisiblePorts());
        }
    }

    private final IMachine machine;
    private final LogisticComponent logistic;
    private final BlockPos subnet;
    private final Runnable onUpdatePorts = () -> needUpdate = true;

    private boolean needUpdate = true;

    public LogisticWorkerMenu(Properties properties) {
        super(properties, MARGIN_X + CONFIG_WIDTH, PANEL_HEIGHT);

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

        addSyncSlot("info", new SyncScheduler());
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
