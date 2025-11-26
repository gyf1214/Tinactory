package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.Boiler;

import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;
import static org.shsts.tinactory.core.machine.Machine.getProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerMenu extends MachineMenu {
    public static final String BURN_SYNC = "burn";
    public static final String HEAT_SYNC = "heat";

    public BoilerMenu(Properties properties) {
        super(properties);
        addProgressSlots(this);
    }

    private static double getBurn(IProcessor processor) {
        var progress = processor.getProgress();
        return progress <= 0 ? 0 : 1 - progress;
    }

    public static void addProgressSlots(MachineMenu menu) {
        menu.addSyncSlot(BURN_SYNC, () -> doublePacket(getProcessor(menu.blockEntity())
            .map(org.shsts.tinactory.content.gui.BoilerMenu::getBurn)
            .orElse(0d)));
        menu.addSyncSlot(HEAT_SYNC, () -> doublePacket(getProcessor(menu.blockEntity())
            .map($ -> ((Boiler) $).getHeat() / 600d)
            .orElse(0d)));
    }
}
