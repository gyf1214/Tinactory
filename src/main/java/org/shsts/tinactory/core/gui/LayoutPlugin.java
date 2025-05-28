package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class LayoutPlugin<S extends MenuScreen> extends InventoryPlugin<S> {
    protected final Layout layout;

    protected LayoutPlugin(IMenu menu, Layout layout, int extraHeight) {
        super(menu, layout.rect.endY() + extraHeight + SPACING);
        this.layout = layout;
    }

    protected LayoutPlugin(IMenu menu, int extraHeight) {
        this(menu, LAYOUT_PROVIDER.get(menu.blockEntity()).getLayout(), extraHeight);
    }

    protected static void addSlots(IMenu menu, Layout layout) {
        var blockEntity = menu.blockEntity();
        var items = MENU_ITEM_HANDLER.get(blockEntity);
        var xOffset = layout.getXOffset();
        for (var slot : layout.slots) {
            var x = xOffset + slot.x() + MARGIN_X + 1;
            var y = slot.y() + MARGIN_TOP + 1;
            if (slot.type().portType == PortType.ITEM) {
                menu.addMenuSlot(new SlotItemHandler(items, slot.index(), x, y));
            }
        }
    }

    public static LayoutPlugin<MenuScreen> simple(IMenu menu) {
        return new LayoutPlugin<>(menu, 0) {
            {
                addSlots(menu, layout);
            }

            @Override
            public Class<MenuScreen> menuScreenClass() {
                return MenuScreen.class;
            }
        };
    }
}
