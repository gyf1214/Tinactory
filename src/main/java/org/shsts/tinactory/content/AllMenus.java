package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.gui.ElectricTankMenu;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.MESignalControllerMenu;
import org.shsts.tinactory.content.gui.MEStorageDetectorMenu;
import org.shsts.tinactory.content.gui.MEStorageInterfaceMenu;
import org.shsts.tinactory.content.gui.MachineMenu;
import org.shsts.tinactory.content.gui.NetworkControllerMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.client.BatteryBoxScreen;
import org.shsts.tinactory.content.gui.client.ElectricChestScreen;
import org.shsts.tinactory.content.gui.client.ElectricTankScreen;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.client.MEDriveScreen;
import org.shsts.tinactory.content.gui.client.MESignalControllerScreen;
import org.shsts.tinactory.content.gui.client.MEStorageDetectorScreen;
import org.shsts.tinactory.content.gui.client.MEStorageInterfaceScreen;
import org.shsts.tinactory.content.gui.client.MachineScreen;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.ProcessingMenu;
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
    /**
     * Used only in special item slots that is not implemented by the vanilla slot system.
     */
    public static final IMenuEvent<SlotEventPacket> ITEM_SLOT_CLICK;
    public static final IMenuEvent<SlotEventPacket> PORT_CLICK;
    public static final IMenuEvent<ISetMachineConfigPacket> SET_MACHINE_CONFIG;
    public static final IMenuEvent<RenameEventPacket> RENAME;
    public static final IMenuEvent<MEStorageInterfaceEventPacket> ME_STORAGE_INTERFACE_SLOT;

    public static final IMenuType WORKBENCH;
    public static final IMenuType NETWORK_CONTROLLER;
    public static final IMenuType BATTERY_BOX;
    public static final IMenuType ELECTRIC_CHEST;
    public static final IMenuType ELECTRIC_TANK;
    public static final IMenuType LOGISTIC_WORKER;
    public static final IMenuType ME_DRIVE;
    public static final IMenuType ME_STORAGE_INTERFACE;
    public static final IMenuType ME_SIGNAL_CONTROLLER;
    public static final IMenuType ME_STORAGE_DETECTOR;
    public static final IMenuType PRIMITIVE_MACHINE;
    public static final IMenuType PROCESSING_MACHINE;
    public static final IMenuType BOILER;
    public static final IMenuType RESEARCH_BENCH;
    public static final IMenuType DIGITAL_INTERFACE;
    public static final IMenuType RESEARCH_DIGITAL_INTERFACE;

    static {
        CHANNEL
            .registerMenuSyncPacket(SyncPackets.DoublePacket.class, SyncPackets.DoublePacket::new)
            .registerMenuSyncPacket(FluidSyncPacket.class, FluidSyncPacket::new)
            .registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new)
            .registerMenuSyncPacket(NetworkControllerSyncPacket.class,
                NetworkControllerSyncPacket::new)
            .registerMenuSyncPacket(LogisticWorkerSyncPacket.class,
                LogisticWorkerSyncPacket::new)
            .registerMenuSyncPacket(MEStorageInterfaceSyncPacket.class,
                MEStorageInterfaceSyncPacket::new)
            .registerMenuSyncPacket(MESignalControllerSyncPacket.class,
                MESignalControllerSyncPacket::new);

        FLUID_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        ITEM_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        PORT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(ISetMachineConfigPacket.class,
            SetMachineConfigPacket::new);
        RENAME = CHANNEL.registerMenuEventPacket(RenameEventPacket.class, RenameEventPacket::new);
        ME_STORAGE_INTERFACE_SLOT = CHANNEL.registerMenuEventPacket(MEStorageInterfaceEventPacket.class,
            MEStorageInterfaceEventPacket::new);

        BATTERY_BOX = REGISTRATE.menu("machine/battery_box", MachineMenu::simpleConfig)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> BatteryBoxScreen::new)
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

        ME_DRIVE = REGISTRATE.menu("logistics/me_drive", MachineMenu::simpleConfig)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MEDriveScreen::new)
            .register();

        ME_STORAGE_INTERFACE = REGISTRATE.menu("logistics/me_storage_interface", MEStorageInterfaceMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MEStorageInterfaceScreen::new)
            .register();

        ME_SIGNAL_CONTROLLER = REGISTRATE.menu("logistics/me_signal_controller", MESignalControllerMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MESignalControllerScreen::new)
            .register();

        ME_STORAGE_DETECTOR = REGISTRATE.menu("logistics/me_storage_detector", MEStorageDetectorMenu::new)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MEStorageDetectorScreen::new)
            .register();

        PRIMITIVE_MACHINE = REGISTRATE.menu("machine/primitive", ProcessingMenu::primitive)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .register();

        PROCESSING_MACHINE = REGISTRATE.menu("machine/processing", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen::new)
            .register();

        BOILER = REGISTRATE.menu("machine/boiler", MachineMenu::boiler)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen.Boiler::new)
            .register();

        RESEARCH_BENCH = REGISTRATE.menu("machine/research_bench", MachineMenu::machine)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ResearchBenchScreen::new)
            .register();

        DIGITAL_INTERFACE = REGISTRATE.menu("multiblock/digital_interface", MachineMenu::digitalInterface)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MachineScreen::new)
            .register();

        RESEARCH_DIGITAL_INTERFACE = REGISTRATE.menu("multiblock/research_digital_interface",
                MachineMenu::digitalInterface)
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> ResearchBenchScreen::new)
            .register();
    }

    public static void init() {}
}
