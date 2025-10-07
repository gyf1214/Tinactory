package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.logistics.PortType;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutMenu extends InventoryMenu {
    protected final Layout layout;

    protected LayoutMenu(Properties properties, Layout layout, int extraHeight) {
        super(properties, layout.rect.endY() + extraHeight);
        this.layout = layout;
    }

    protected LayoutMenu(Properties properties, int extraHeight) {
        this(properties,
            LAYOUT_PROVIDER.get(properties.blockEntity()).getLayout(),
            extraHeight);
    }

    protected void addLayoutSlots(Layout layout) {
        var items = MENU_ITEM_HANDLER.get(blockEntity);
        var xOffset = layout.getXOffset();
        for (var slot : layout.slots) {
            var x = xOffset + slot.x() + MARGIN_X + 1;
            var y = slot.y() + MARGIN_TOP + 1;
            if (slot.type().portType == PortType.ITEM) {
                addSlot(new SlotItemHandler(items, slot.index(), x, y));
            }
        }
    }

    public Layout layout() {
        return layout;
    }
}
