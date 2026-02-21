package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.NuclearReactor;
import org.shsts.tinactory.core.gui.ProcessingMenu;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NuclearReactorMenu extends MachineMenu {
    public static final String HEAT_SYNC = BoilerMenu.HEAT_SYNC;
    private static final int REACTOR_SLOT_WIDTH = SLOT_SIZE * 5;
    private static final int REACTOR_SLOT_HEIGHT = SLOT_SIZE * 5;

    public NuclearReactorMenu(Properties properties) {
        super(properties);
        addReactorSlots(this, machine);
    }

    public static void addReactorSlots(ProcessingMenu menu, IMachine machine) {
        var nuclearReactor = (NuclearReactor) machine.processor().orElseThrow();

        var reactorItems = nuclearReactor.reactorItems();
        var rows = nuclearReactor.rows();
        var columns = nuclearReactor.columns();
        var xOffset = (REACTOR_SLOT_WIDTH - rows * SLOT_SIZE) / 2;
        var yOffset = (REACTOR_SLOT_HEIGHT - columns * SLOT_SIZE) / 2;
        for (var i = 0; i < rows; i++) {
            for (var j = 0; j < columns; j++) {
                var x = j * SLOT_SIZE + xOffset + MARGIN_X + 1;
                var y = i * SLOT_SIZE + yOffset + MARGIN_TOP + 1;
                menu.addSlot(new SlotItemHandler(reactorItems, i * columns + j, x, y));
            }
        }

        menu.addSyncSlot(HEAT_SYNC, () -> doublePacket(nuclearReactor.heatProgress()));
    }

    public static class DigitalInterface extends DigitalInterfaceMenu {
        public DigitalInterface(Properties properties) {
            super(properties);
            addReactorSlots(this, machine);
        }
    }
}
