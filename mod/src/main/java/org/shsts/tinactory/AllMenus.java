package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.gui.ElectricTankMenu;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.MECraftTerminalMenu;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.MESignalControllerMenu;
import org.shsts.tinactory.content.gui.MEStorageDetectorMenu;
import org.shsts.tinactory.content.gui.MEStorageInterfaceMenu;
import org.shsts.tinactory.content.gui.MachineMenu;
import org.shsts.tinactory.content.gui.TechMenu;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.client.BatteryBoxScreen;
import org.shsts.tinactory.content.gui.client.BoilerScreen;
import org.shsts.tinactory.content.gui.client.ElectricChestScreen;
import org.shsts.tinactory.content.gui.client.ElectricTankScreen;
import org.shsts.tinactory.content.gui.client.FusionScreen;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.client.MECraftTerminalScreen;
import org.shsts.tinactory.content.gui.client.MEDriveScreen;
import org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen;
import org.shsts.tinactory.content.gui.client.MESignalControllerScreen;
import org.shsts.tinactory.content.gui.client.MEStorageDetectorScreen;
import org.shsts.tinactory.content.gui.client.MEStorageInterfaceScreen;
import org.shsts.tinactory.content.gui.client.MachineScreen;
import org.shsts.tinactory.content.gui.client.NuclearReactorScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.gui.client.TechScreen;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
import org.shsts.tinactory.content.gui.sync.MECraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftRequestSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.content.gui.sync.OpenTechPacket;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.integration.gui.ProcessingMenu;
import org.shsts.tinactory.integration.gui.sync.FluidSyncPacket;
import org.shsts.tinycorelib.api.gui.IMenuEvent;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.builder.IMenuBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.function.Function;

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
    public static final IMenuEvent<MECraftEventPacket> ME_CRAFT_ACTION;
    public static final IMenuEvent<MEPatternEventPacket> ME_PATTERN_ACTION;

    public static final IMenuType WORKBENCH;
    public static final IMenuType TECH_MENU;
    public static final IMenuType BATTERY_BOX;
    public static final IMenuType ELECTRIC_CHEST;
    public static final IMenuType ELECTRIC_TANK;
    public static final IMenuType LOGISTIC_WORKER;
    public static final IMenuType ME_DRIVE;
    public static final IMenuType ME_STORAGE_INTERFACE;
    public static final IMenuType ME_SIGNAL_CONTROLLER;
    public static final IMenuType ME_STORAGE_DETECTOR;
    public static final IMenuType ME_CRAFT_TERMINAL;
    public static final IMenuType ME_PATTERN_TERMINAL;
    public static final IMenuType PRIMITIVE_MACHINE;
    public static final IMenuType PROCESSING_MACHINE;
    public static final IMenuType BOILER;
    public static final IMenuType RESEARCH_BENCH;
    public static final IMenuType NUCLEAR_REACTOR;
    public static final IMenuType FUSION;
    public static final IMenuType DIGITAL_INTERFACE;
    public static final IMenuType RESEARCH_DIGITAL_INTERFACE;
    public static final IMenuType BOILER_DIGITAL_INTERFACE;
    public static final IMenuType NUCLEAR_REACTOR_DIGITAL_INTERFACE;
    public static final IMenuType FUSION_DIGITAL_INTERFACE;

    static {
        CHANNEL
            .registerMenuSyncPacket(SyncPackets.DoublePacket.class, SyncPackets.DoublePacket::new)
            .registerMenuSyncPacket(SyncPackets.LongPacket.class, SyncPackets.LongPacket::new)
            .registerMenuSyncPacket(SyncPackets.UnitPacket.class, () -> SyncPackets.UnitPacket.INSTANCE)
            .registerMenuSyncPacket(FluidSyncPacket.class, FluidSyncPacket::new)
            .registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new)
            .registerMenuSyncPacket(LogisticWorkerSyncPacket.class,
                LogisticWorkerSyncPacket::new)
            .registerMenuSyncPacket(MEStorageInterfaceSyncPacket.class,
                MEStorageInterfaceSyncPacket::new)
            .registerMenuSyncPacket(MESignalControllerSyncPacket.class,
                MESignalControllerSyncPacket::new)
            .registerMenuSyncPacket(MECraftRequestSyncPacket.class,
                MECraftRequestSyncPacket::new)
            .registerMenuSyncPacket(MECraftCpuSyncPacket.class,
                MECraftCpuSyncPacket::new)
            .registerMenuSyncPacket(MECraftPreviewSyncPacket.class,
                MECraftPreviewSyncPacket::new)
            .registerMenuSyncPacket(MEPatternSyncPacket.class,
                MEPatternSyncPacket::new);

        FLUID_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        ITEM_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        PORT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class, SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(ISetMachineConfigPacket.class,
            SetMachineConfigPacket::new);
        RENAME = CHANNEL.registerMenuEventPacket(RenameEventPacket.class, RenameEventPacket::new);
        ME_STORAGE_INTERFACE_SLOT = CHANNEL.registerMenuEventPacket(MEStorageInterfaceEventPacket.class,
            MEStorageInterfaceEventPacket::new);
        ME_CRAFT_ACTION = CHANNEL.registerMenuEventPacket(MECraftEventPacket.class,
            MECraftEventPacket::new);
        ME_PATTERN_ACTION = CHANNEL.registerMenuEventPacket(MEPatternEventPacket.class,
            MEPatternEventPacket::new);

        CHANNEL.registerPacket(OpenTechPacket.class, () -> OpenTechPacket.INSTANCE, TechMenu::onOpenGui);

        BATTERY_BOX = processing("machine/battery_box", MachineMenu::simpleConfig)
            .screen(() -> () -> BatteryBoxScreen::new)
            .register();

        WORKBENCH = REGISTRATE.menu("primitive/workbench", WorkbenchMenu::new)
            .title("block.tinactory.primitive.workbench")
            .screen(() -> () -> WorkbenchScreen::new)
            .register();

        ELECTRIC_CHEST = processing("machine/electric_chest", ElectricChestMenu::new)
            .screen(() -> () -> ElectricChestScreen::new)
            .register();

        ELECTRIC_TANK = processing("machine/electric_tank", ElectricTankMenu::new)
            .screen(() -> () -> ElectricTankScreen::new)
            .register();

        TECH_MENU = REGISTRATE.menu("network/controller", TechMenu::new)
            .title("tinactory.gui.techMenu.title")
            .screen(() -> () -> TechScreen::new)
            .register();

        LOGISTIC_WORKER = processing("logistics/logistic_worker", LogisticWorkerMenu::new)
            .screen(() -> () -> LogisticWorkerScreen::new)
            .register();

        ME_DRIVE = processing("logistics/me_drive", MachineMenu::simpleConfig)
            .screen(() -> () -> MEDriveScreen::new)
            .register();

        ME_STORAGE_INTERFACE = processing("logistics/me_storage_interface", MEStorageInterfaceMenu::new)
            .screen(() -> () -> MEStorageInterfaceScreen::new)
            .register();

        ME_SIGNAL_CONTROLLER = processing("logistics/me_signal_controller", MESignalControllerMenu::new)
            .screen(() -> () -> MESignalControllerScreen::new)
            .register();

        ME_STORAGE_DETECTOR = processing("logistics/me_storage_detector", MEStorageDetectorMenu::new)
            .screen(() -> () -> MEStorageDetectorScreen::new)
            .register();

        ME_CRAFT_TERMINAL = processing("logistics/me_craft_terminal", MECraftTerminalMenu::new)
            .screen(() -> () -> MECraftTerminalScreen::new)
            .register();

        ME_PATTERN_TERMINAL = processing("logistics/me_pattern_terminal", MEPatternTerminalMenu::new)
            .screen(() -> () -> MEPatternTerminalScreen::new)
            .register();

        PRIMITIVE_MACHINE = processing("machine/primitive", ProcessingMenu::primitive)
            .screen(() -> () -> ProcessingScreen::new)
            .register();

        PROCESSING_MACHINE = processing("machine/processing", MachineMenu::machine)
            .screen(() -> () -> MachineScreen::new)
            .register();

        BOILER = processing("machine/boiler", MachineMenu::boiler)
            .screen(() -> () -> BoilerScreen::new)
            .register();

        RESEARCH_BENCH = processing("machine/research_bench", MachineMenu::machine)
            .screen(() -> () -> ResearchBenchScreen::new)
            .register();

        NUCLEAR_REACTOR = processing("machine/nuclear_reactor", MachineMenu::nuclearReactor)
            .screen(() -> () -> NuclearReactorScreen::new)
            .register();

        FUSION = processing("multiblock/fusion", MachineMenu::fusion)
            .screen(() -> () -> FusionScreen::new)
            .register();

        DIGITAL_INTERFACE = processing("multiblock/digital_interface", MachineMenu::digitalInterface)
            .screen(() -> () -> MachineScreen::new)
            .register();

        RESEARCH_DIGITAL_INTERFACE = processing("multiblock/digital_interface/research",
            MachineMenu::digitalInterface)
            .screen(() -> () -> ResearchBenchScreen::new)
            .register();

        BOILER_DIGITAL_INTERFACE = processing("multiblock/digital_interface/boiler",
            MachineMenu::boilerDigitalInterface)
            .screen(() -> () -> BoilerScreen::new)
            .register();

        NUCLEAR_REACTOR_DIGITAL_INTERFACE = processing("multiblock/digital_interface/nuclear_reactor",
            MachineMenu::nuclearReactorDigitalInterface)
            .screen(() -> () -> NuclearReactorScreen::new)
            .register();

        FUSION_DIGITAL_INTERFACE = processing("multiblock/digital_interface/fusion",
            MachineMenu::fusionDigitalInterface)
            .screen(() -> () -> FusionScreen::new)
            .register();
    }

    public static <M extends MenuBase> IMenuBuilder<M, IRegistrate> processing(
        String id, Function<MenuBase.Properties, M> factory) {
        return REGISTRATE.menu(id, factory).title(ProcessingMenu::getTitle);
    }

    public static void init() {}
}
