package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.multiblock.FusionRuntime;
import org.shsts.tinactory.core.util.MathUtil;

import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionMenu extends MachineMenu {
    public static final String ENERGY_SYNC = "startup_energy";

    public FusionMenu(Properties properties) {
        super(properties);
        addProgressSlots(this);
    }

    public static double getStartupEnergy(IProcessor processor) {
        var runtime = (FusionRuntime) processor;
        return MathUtil.clamp(runtime.startupEnergy() / runtime.startupCapacity(), 0d, 1d);
    }

    public static void addProgressSlots(MachineMenu menu) {
        menu.addSyncSlot(ENERGY_SYNC, () -> doublePacket(getProcessor(menu.blockEntity())
            .map(FusionMenu::getStartupEnergy)
            .orElse(0d)));
    }

    public static class DigitalInterface extends DigitalInterfaceMenu {
        public DigitalInterface(Properties properties) {
            super(properties);
            addProgressSlots(this);
        }
    }
}
