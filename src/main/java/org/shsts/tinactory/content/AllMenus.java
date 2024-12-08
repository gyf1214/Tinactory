package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.gui.ElectricChestPlugin;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.ChestItemSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinycorelib.api.gui.IMenuEvent;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.Tinactory.CHANNEL;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMenus {
    public static final IMenuEvent<SlotEventPacket> CHEST_SLOT_CLICK;
    public static final IMenuEvent<SetMachineConfigPacket> SET_MACHINE_CONFIG;

    public static final IMenuType ELECTRIC_CHEST;

    static {
        CHANNEL.registerMenuSyncPacket(ChestItemSyncPacket.class, ChestItemSyncPacket::new);

        CHEST_SLOT_CLICK = CHANNEL.registerMenuEventPacket(SlotEventPacket.class,
            SlotEventPacket::new);
        SET_MACHINE_CONFIG = CHANNEL.registerMenuEventPacket(SetMachineConfigPacket.class,
            SetMachineConfigPacket::new);

        ELECTRIC_CHEST = REGISTRATE.menu("machine/" + "/electric_chest")
            .title(ProcessingMenu::getTitle)
            .screen(() -> () -> MenuScreen::new)
            .plugin(ElectricChestPlugin::new)
            .register();
    }

    public static void init() {}
}
