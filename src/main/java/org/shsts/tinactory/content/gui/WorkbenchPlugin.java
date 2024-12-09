package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.WorkbenchResult;
import org.shsts.tinactory.core.gui.InventoryPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.WORKBENCH;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchPlugin extends InventoryPlugin<WorkbenchScreen> {
    private static final Layout LAYOUT = AllLayouts.WORKBENCH;

    public WorkbenchPlugin(IMenu menu) {
        super(menu, LAYOUT.rect.endY() + SPACING);
        var workbench = WORKBENCH.get(menu.blockEntity());
        var container = MENU_ITEM_HANDLER.get(menu.blockEntity());
        var xOffset = LAYOUT.getXOffset();
        var outputX = 0;
        var outputY = 0;
        for (var slot : LAYOUT.slots) {
            var x = xOffset + slot.x();
            var y = slot.y();
            if (slot.type() == SlotType.NONE) {
                outputX = x;
                outputY = y;
            } else if (slot.type() == SlotType.ITEM_INPUT) {
                menu.addSlot(new SlotItemHandler(container, slot.index(),
                    x + MARGIN_HORIZONTAL + 1, y + MARGIN_TOP + 1));
            } else {
                throw new IllegalArgumentException();
            }
        }
        menu.addSlot(new WorkbenchResult(workbench,
            outputX + MARGIN_HORIZONTAL + 1, outputY + MARGIN_TOP + 1));
    }

    @Override
    protected boolean onQuickMoveStack(Slot slot) {
        var oldStack = slot.getItem().copy();
        if (!super.onQuickMoveStack(slot)) {
            return false;
        }
        return StackHelper.itemStackEqual(oldStack, slot.getItem());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<WorkbenchScreen> menuScreenClass() {
        return WorkbenchScreen.class;
    }
}
