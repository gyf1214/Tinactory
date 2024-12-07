package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;

import static org.shsts.tinactory.content.AllCapabilities.LOGISTIC_WORKER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerMenu extends Menu<SmartBlockEntity, LogisticWorkerMenu> {
    public final MachineConfig machineConfig;
    public final int syncSlot;

    public LogisticWorkerMenu(SmartMenuType<SmartBlockEntity, ?> type, int id,
        Inventory inventory, SmartBlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);
        var machine = AllCapabilities.MACHINE.get(blockEntity);
        this.machineConfig = machine.config;
        this.syncSlot = addSyncSlot(LogisticWorkerSyncPacket::new);

        onEventPacket(MenuEventHandler.SET_MACHINE_CONFIG, machine::setConfig);

        machine.forceUpdate();
        blockEntity.forceAndSendUpdate();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen<LogisticWorkerMenu> createScreen(Inventory inventory, Component title) {
        var slots = LOGISTIC_WORKER.get(blockEntity).workerSlots;
        return new LogisticWorkerScreen(this, inventory, title, slots);
    }
}
