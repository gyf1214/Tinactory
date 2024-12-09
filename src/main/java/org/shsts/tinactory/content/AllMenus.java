package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.gui.ElectricChestPlugin;
import org.shsts.tinactory.content.gui.ElectricTankPlugin;
import org.shsts.tinactory.content.gui.WorkbenchPlugin;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.NetworkControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinycorelib.api.gui.IMenuEvent;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.Tinactory.CHANNEL;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMenus {
    public static final IMenuEvent<SlotEventPacket> FLUID_SLOT_CLICK;
    public static final IMenuEvent<SlotEventPacket> CHEST_SLOT_CLICK;
    public static final IMenuEvent<SetMachineConfigPacket> SET_MACHINE_CONFIG;

    public static final IMenuType WORKBENCH;
    public static final IMenuType NETWORK_CONTROLLER;
    public static final IMenuType ELECTRIC_CHEST;
    public static final IMenuType ELECTRIC_TANK;

    static {
        CHANNEL
            .registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new)
            .registerMenuSyncPacket(FluidSyncPacket.class, FluidSyncPacket::new)
            .registerMenuSyncPacket(NetworkControllerSyncPacket.class,
                NetworkControllerSyncPacket::new);

        FLUID_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        CHEST_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(SetMachineConfigPacket.class,
            SetMachineConfigPacket::new);

        WORKBENCH = REGISTRATE.menu("primitive/workbench")
            .title("tinactory.gui.networkController.title")
            .screen(() -> () -> WorkbenchScreen::new)
            .plugin(WorkbenchPlugin::new)
            .register();

        ELECTRIC_CHEST = REGISTRATE.menu("machine/electric_chest")
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(ElectricChestPlugin::new)
            .register();

        ELECTRIC_TANK = REGISTRATE.menu("machine/electric_tank")
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(ElectricTankPlugin::new)
            .register();

        NETWORK_CONTROLLER = REGISTRATE.menu("network/controller")
            .title("tinactory.gui.networkController.title")
            .screen(() -> () -> NetworkControllerScreen::new)
            .dummyPlugin(menu -> {
                menu.setValidPredicate($ -> AllCapabilities.NETWORK_CONTROLLER
                    .get(menu.blockEntity())
                    .canPlayerInteract(menu.player()));
                menu.addSyncSlot("info", NetworkControllerSyncPacket::new);
            })
            .register();
    }

    public static void init() {}
}
