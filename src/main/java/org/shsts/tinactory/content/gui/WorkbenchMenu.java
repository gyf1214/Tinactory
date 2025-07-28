package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.Slot;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.gui.sync.WorkbenchResult;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.logistics.StackHelper;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends LayoutMenu {
    public WorkbenchMenu(Properties properties) {
        super(properties, AllLayouts.WORKBENCH, 0);
        addLayoutSlots(layout);

        var workbench = Workbench.get(blockEntity);
        for (var slot : layout.slots) {
            if (slot.type() == SlotType.NONE) {
                var x = slot.x() + layout.getXOffset() + MARGIN_X + 1;
                var y = slot.y() + MARGIN_TOP + 1;
                addSlot(new WorkbenchResult(workbench, x, y));
            }
        }
    }

    @Override
    protected boolean quickMoveStack(Slot slot) {
        var oldStack = slot.getItem().copy();
        if (!super.quickMoveStack(slot)) {
            return false;
        }
        return StackHelper.itemStackEqual(oldStack, slot.getItem());
    }
}
