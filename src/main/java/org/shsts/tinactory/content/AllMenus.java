package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.gui.ElectricTankMenu;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.MEStorageInterfaceMenu;
import org.shsts.tinactory.content.gui.MachineMenu;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.client.ElectricChestScreen;
import org.shsts.tinactory.content.gui.client.ElectricTankScreen;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.client.MEStorageInterfaceScreen;
import org.shsts.tinactory.content.gui.client.MachineScreen;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.LayoutScreen;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinycorelib.api.gui.IMenuEvent;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.Tinactory.CHANNEL;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMenus {
    public static final IMenuEvent<SlotEventPacket> FLUID_SLOT_CLICK;
    public static final IMenuEvent<SlotEventPacket> CHEST_SLOT_CLICK;
    public static final IMenuEvent<ISetMachineConfigPacket> SET_MACHINE_CONFIG;
    public static final IMenuEvent<RenameEventPacket> RENAME;
    public static final IMenuEvent<MEStorageInterfaceEventPacket> ME_STORAGE_INTERFACE_SLOT;

    public static final IMenuType WORKBENCH;
    public static final IMenuType NETWORK_CONTROLLER;
    public static final IMenuType SIMPLE_MACHINE;
    public static final IMenuType ELECTRIC_CHEST;
    public static final IMenuType ELECTRIC_TANK;
    public static final IMenuType LOGISTIC_WORKER;
    public static final IMenuType ME_STORAGE_INTERFACE;
    public static final IMenuType PRIMITIVE_MACHINE;
    public static final IMenuType PROCESSING_MACHINE;
    public static final IMenuType MARKER;
    public static final IMenuType MARKER_WITH_NORMAL;
    public static final IMenuType BOILER;
    public static final IMenuType ELECTRIC_FURNACE;
    public static final IMenuType RESEARCH_BENCH;
    public static final IMenuType MULTIBLOCK;

    static {
        CHANNEL
            .registerMenuSyncPacket(SyncPackets.Double.class, SyncPackets.Double::new)
            .registerMenuSyncPacket(FluidSyncPacket.class, FluidSyncPacket::new)
            .registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new)
            .registerMenuSyncPacket(NetworkControllerSyncPacket.class,
                NetworkControllerSyncPacket::new)
            .registerMenuSyncPacket(LogisticWorkerSyncPacket.class,
                LogisticWorkerSyncPacket::new)
            .registerMenuSyncPacket(MEStorageInterfaceSyncPacket.class,
                MEStorageInterfaceSyncPacket::new);

        FLUID_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        CHEST_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(ISetMachineConfigPacket.class,
            SetMachineConfigPacket::new);
        RENAME = CHANNEL.registerMenuEventPacket(RenameEventPacket.class,
            RenameEventPacket::new);
        ME_STORAGE_INTERFACE_SLOT = CHANNEL.registerMenuEventPacket(MEStorageInterfaceEventPacket.class,
            MEStorageInterfaceEventPacket::new);

        SIMPLE_MACHINE = REGISTRATE.menu("machine/simple", MachineMenu::simple)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> LayoutScreen.Simple::new)
            .register();

        WORKBENCH = REGISTRATE.menu("primitive/workbench", WorkbenchMenu::new)
            .title("block.tinactory.primitive.workbench")
            .screen(() -> () -> WorkbenchScreen::new)
            .register();

        ELECTRIC_CHEST = REGISTRATE.menu("machine/electric_chest", ElectricChestMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ElectricChestScreen::new)
            .register();

        ELECTRIC_TANK = REGISTRATE.menu("machine/electric_tank", ElectricTankMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ElectricTankScreen::new)
            .register();

        NETWORK_CONTROLLER = REGISTRATE.menu("network/controller", NetworkControllerMenu::new)
            .title("tinactory.gui.networkController.title")
            .screen(() -> () -> NetworkControllerScreen::new)
            .register();

        LOGISTIC_WORKER = REGISTRATE.menu("logistics/logistic_worker", LogisticWorkerMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> LogisticWorkerScreen::new)
            .register();

        ME_STORAGE_INTERFACE = REGISTRATE.menu("logistics/me_storage_interface", MEStorageInterfaceMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MEStorageInterfaceScreen::new)
            .register();

        PRIMITIVE_MACHINE = REGISTRATE.menu("machine/primitive", ProcessingMenu::primitive)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .register();

        PROCESSING_MACHINE = REGISTRATE.menu("machine/processing", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen::new)
            .register();

        MARKER = REGISTRATE.menu("machine/marker", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen.marker(false))
            .register();

        MARKER_WITH_NORMAL = REGISTRATE.menu("machine/marker_with_normal", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen.marker(true))
            .register();

        BOILER = REGISTRATE.menu("machine/boiler", MachineMenu::boiler)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen.Boiler::new)
            .register();

        ELECTRIC_FURNACE = REGISTRATE.menu("machine/electric_furnace", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen.ElectricFurnace::new)
            .register();

        RESEARCH_BENCH = REGISTRATE.menu("machine/research_bench", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ResearchBenchScreen::new)
            .register();

        MULTIBLOCK = REGISTRATE.menu("multiblock", MachineMenu::multiblock)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen::new)
            .register();
    }

    public static void init() {}
}
