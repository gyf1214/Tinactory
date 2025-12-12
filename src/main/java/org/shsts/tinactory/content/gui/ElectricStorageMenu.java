package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.core.gui.LayoutMenu;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricStorageMenu extends LayoutMenu {
    private final IMachine machine;
    private final IMachineConfig machineConfig;

    protected ElectricStorageMenu(Properties properties) {
        super(properties, SLOT_SIZE + SPACING);
        this.machine = MACHINE.get(blockEntity());
        this.machineConfig = machine.config();

        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    public IMachineConfig machineConfig() {
        return machineConfig;
    }
}
