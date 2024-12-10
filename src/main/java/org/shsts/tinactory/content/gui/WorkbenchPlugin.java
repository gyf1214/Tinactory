package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.content.gui.sync.WorkbenchResult;
import org.shsts.tinactory.core.gui.LayoutPlugin;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.WORKBENCH;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchPlugin extends LayoutPlugin<WorkbenchScreen> {
    public WorkbenchPlugin(IMenu menu) {
        super(menu, AllLayouts.WORKBENCH, 0);
        addSlots(menu, layout);

        var workbench = WORKBENCH.get(menu.blockEntity());
        for (var slot : layout.slots) {
            if (slot.type() == SlotType.NONE) {
                var x = slot.x() + layout.getXOffset() + MARGIN_X + 1;
                var y = slot.y() + MARGIN_TOP + 1;
                menu.addSlot(new WorkbenchResult(workbench, x, y));
            }
        }
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
