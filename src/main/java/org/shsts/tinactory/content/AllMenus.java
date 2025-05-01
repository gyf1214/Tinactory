package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.content.gui.ElectricChestPlugin;
import org.shsts.tinactory.content.gui.ElectricTankPlugin;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.gui.PrimitivePlugin;
import org.shsts.tinactory.content.gui.WorkbenchPlugin;
import org.shsts.tinactory.content.gui.client.BoilerScreen;
import org.shsts.tinactory.content.gui.client.LogisticWorkerScreen;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.core.gui.LayoutPlugin;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.gui.IMenuEvent;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.Tinactory.CHANNEL;
import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMenus {
    public static final IMenuEvent<SlotEventPacket> FLUID_SLOT_CLICK;
    public static final IMenuEvent<SlotEventPacket> CHEST_SLOT_CLICK;
    public static final IMenuEvent<ISetMachineConfigPacket> SET_MACHINE_CONFIG;

    public static final IMenuType WORKBENCH;
    public static final IMenuType NETWORK_CONTROLLER;
    public static final IMenuType ELECTRIC_CHEST;
    public static final IMenuType ELECTRIC_TANK;
    public static final IMenuType LOGISTIC_WORKER;
    public static final IMenuType PRIMITIVE_MACHINE;
    public static final IMenuType PROCESSING_MACHINE;
    public static final IMenuType MARKER;
    public static final IMenuType MARKER_WITH_NORMAL;
    public static final IMenuType BOILER;
    public static final IMenuType ELECTRIC_FURNACE;
    public static final IMenuType RESEARCH_BENCH;
    public static final IMenuType BATTERY_BOX;
    public static final IMenuType MULTIBLOCK;

    static {
        CHANNEL
            .registerMenuSyncPacket(SyncPackets.Double.class, SyncPackets.Double::new)
            .registerMenuSyncPacket(FluidSyncPacket.class, FluidSyncPacket::new)
            .registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new)
            .registerMenuSyncPacket(NetworkControllerSyncPacket.class,
                NetworkControllerSyncPacket::new)
            .registerMenuSyncPacket(LogisticWorkerSyncPacket.class,
                LogisticWorkerSyncPacket::new);

        FLUID_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        CHEST_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(ISetMachineConfigPacket.class,
            SetMachineConfigPacket::new);

        WORKBENCH = REGISTRATE.menu("primitive/workbench")
            .title("tinactory.gui.workbench.title")
            .screen(() -> () -> WorkbenchScreen::new)
            .plugin(WorkbenchPlugin::new)
            .register();

        ELECTRIC_CHEST = REGISTRATE.menu("machine/electric_chest")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(ElectricChestPlugin::new)
            .register();

        ELECTRIC_TANK = REGISTRATE.menu("machine/electric_tank")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(ElectricTankPlugin::new)
            .register();

        NETWORK_CONTROLLER = REGISTRATE.menu("network/controller")
            .title("tinactory.gui.networkController.title")
            .screen(() -> () -> NetworkControllerScreen::new)
            .dummyPlugin(menu -> {
                menu.setValidPredicate(() -> NetworkController
                    .get(menu.blockEntity())
                    .canPlayerInteract(menu.player()));
                menu.addSyncSlot("info", NetworkControllerSyncPacket::new);
            })
            .register();

        LOGISTIC_WORKER = REGISTRATE.menu("network/logistic_worker")
            .title("tinactory.gui.logisticWorker.title")
            .screen(() -> () -> LogisticWorkerScreen::new)
            .dummyPlugin(menu -> {
                menu.addSyncSlot("info", LogisticWorkerSyncPacket::new);
                menu.onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(menu.blockEntity()).setConfig(p));
            })
            .register();

        PRIMITIVE_MACHINE = REGISTRATE.menu("machine/primitive")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(PrimitivePlugin::new)
            .register();

        PROCESSING_MACHINE = REGISTRATE.menu("machine/processing")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(MachinePlugin::new)
            .register();

        MARKER = REGISTRATE.menu("machine/marker")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(MachinePlugin.marker(false))
            .register();

        MARKER_WITH_NORMAL = REGISTRATE.menu("machine/marker_with_normal")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(MachinePlugin.marker(true))
            .register();

        BOILER = REGISTRATE.menu("machine/boiler")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> BoilerScreen::new)
            .plugin(MachinePlugin::noBook)
            .dummyPlugin(menu -> {
                menu.addSyncSlot("burn", be -> new SyncPackets.Double(Machine.getProcessor(be)
                    .map(IProcessor::getProgress).orElse(0d)));
                menu.addSyncSlot("heat", be -> new SyncPackets.Double(Machine.getProcessor(be)
                    .map($ -> ((Boiler) $).getHeat() / 500d).orElse(0d)));
            })
            .register();

        ELECTRIC_FURNACE = REGISTRATE.menu("machine/electric_furnace")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(MachinePlugin::electricFurnace)
            .register();

        RESEARCH_BENCH = REGISTRATE.menu("machine/research_bench")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ResearchBenchScreen::new)
            .plugin(MachinePlugin::new)
            .register();

        BATTERY_BOX = REGISTRATE.menu("machine/battery_box")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(LayoutPlugin::simple)
            .register();

        MULTIBLOCK = REGISTRATE.menu("multiblock")
            .title(ProcessingPlugin::getTitle)
            .screen(() -> () -> ProcessingScreen::new)
            .plugin(MachinePlugin::multiblock)
            .register();
    }

    public static void init() {}
}
