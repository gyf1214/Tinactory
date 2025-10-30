package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.logistics.SignalComponent;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.MACHINE_COMPARATOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MESignalControllerMenu extends MenuBase {
    public static final String SIGNAL_SYNC = "signals";

    public final IMachine machine;
    private final SignalComponent signals;
    private final BlockPos subnet;
    private final Runnable onUpdatePorts;

    public MESignalControllerMenu(Properties properties) {
        super(properties);

        var scheduler = new ActiveScheduler<>(() ->
            new MESignalControllerSyncPacket(getVisibleSignals()));
        this.onUpdatePorts = scheduler::invokeUpdate;

        this.machine = MACHINE.get(blockEntity);
        if (!world.isClientSide) {
            var network = machine.network().orElseThrow();
            this.signals = network.getComponent(SIGNAL_COMPONENT.get());
            this.subnet = network.getSubnet(blockEntity.getBlockPos());
            signals.onUpdate(onUpdatePorts);
        } else {
            this.signals = null;
            this.subnet = null;
        }

        addSyncSlot(SIGNAL_SYNC, scheduler);
        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!world.isClientSide) {
            signals.unregisterCallback(onUpdatePorts);
        }
    }

    private List<MESignalControllerSyncPacket.SignalInfo> getVisibleSignals() {
        var infos = signals.getSubnetSignals(subnet).stream()
            .sorted(Comparator.comparing(SignalComponent.SignalInfo::machine, MACHINE_COMPARATOR))
            .toList();

        var ret = new ArrayList<MESignalControllerSyncPacket.SignalInfo>();
        for (var info : infos) {
            var machine = info.machine();
            ret.add(new MESignalControllerSyncPacket.SignalInfo(machine.uuid(),
                machine.title(), machine.icon(), info.key(), info.isWrite()));
        }
        return ret;
    }
}
