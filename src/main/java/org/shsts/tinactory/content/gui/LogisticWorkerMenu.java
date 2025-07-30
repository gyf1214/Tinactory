package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.core.gui.ProcessingMenu.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends MenuBase {
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
        super(properties);

        this.machine = MACHINE.get(blockEntity);
        if (!world.isClientSide) {
            var network = machine.network().orElseThrow();
            this.logistic = network.getComponent(LOGISTIC_COMPONENT.get());
            this.subnet = network.getSubnet(blockEntity.getBlockPos());
            logistic.onUpdatePorts(onUpdatePorts);
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
