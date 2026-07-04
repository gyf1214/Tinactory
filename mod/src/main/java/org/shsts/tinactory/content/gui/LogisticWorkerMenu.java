package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.integration.gui.InventoryMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.LOGISTIC_WORKER_SYNC;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.integration.gui.ProcessingMenu.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends InventoryMenu {
    public static final int CONFIG_WIDTH = BUTTON_SIZE * 4 + 2;
    public static final String SLOT_SYNC = "info";

    public static final Comparator<IMachine> MACHINE_COMPARATOR =
        Comparator.<IMachine>comparingLong($ -> $.electric().map(IElectricMachine::getVoltage).orElse(0L))
            .thenComparing($ -> BuiltInRegistries.BLOCK.getKey($.blockEntity().getBlockState().getBlock()))
            .thenComparing($ -> $.title().getString());

    public final IMachine machine;
    @Nullable
    private final LogisticComponent logistic;
    private final Runnable onUpdatePorts;

    public LogisticWorkerMenu(Properties properties) {
        super(properties, MARGIN_X + CONFIG_WIDTH, Menu.PANEL_HEIGHT);

        var scheduler = new ActiveScheduler<>(LOGISTIC_WORKER_SYNC, () ->
            new LogisticWorkerSyncPacket(getVisiblePorts()));
        this.onUpdatePorts = scheduler::invokeUpdate;
        addSyncSlot(SLOT_SYNC, scheduler);

        this.machine = MACHINE.get(blockEntity());

        var network = machine.network();
        if (network.isPresent()) {
            this.logistic = network.get().getComponent(LOGISTIC_COMPONENT.get());
            logistic.onUpdate(onUpdatePorts);
        } else {
            this.logistic = null;
        }

        onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(blockEntity()).setConfig(p));
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (logistic != null) {
            logistic.unregisterCallback(onUpdatePorts);
        }
    }

    private List<LogisticWorkerSyncPacket.PortInfo> getVisiblePorts() {
        if (logistic == null) {
            return Collections.emptyList();
        }

        var infos = logistic.getVisiblePorts(machine).stream()
            .sorted(Comparator.comparing(LogisticComponent.PortInfo::machine, MACHINE_COMPARATOR))
            .toList();

        var ret = new ArrayList<LogisticWorkerSyncPacket.PortInfo>();
        for (var info : infos) {
            var machine1 = info.machine();
            var index = info.portIndex();
            var portName = portLabel(info.port().type(), index);

            ret.add(new LogisticWorkerSyncPacket.PortInfo(machine1.uuid(),
                index, machine1.title(), machine1.icon(), portName));
        }
        return ret;
    }
}
