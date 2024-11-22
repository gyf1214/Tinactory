package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends Menu<LogisticWorker, LogisticWorkerMenu> {
    public final MachineConfig machineConfig;
    public final int syncSlot;

    public LogisticWorkerMenu(SmartMenuType<LogisticWorker, ?> type, int id,
        Inventory inventory, LogisticWorker blockEntity) {
        super(type, id, inventory, blockEntity);
        var machine = AllCapabilities.MACHINE.get(blockEntity);
        this.machineConfig = machine.config;
        this.syncSlot = addSyncSlot(LogisticWorkerSyncPacket::new);

        onEventPacket(MenuEventHandler.SET_MACHINE_CONFIG, machine::setConfig);

        machine.forceUpdate();
        blockEntity.forceAndSendUpdate();
    }

    @Override
    public MenuScreen<LogisticWorkerMenu> createScreen(Inventory inventory, Component title) {
        return new LogisticWorkerScreen(this, inventory, title, blockEntity.getConfigSlots());
    }
}
