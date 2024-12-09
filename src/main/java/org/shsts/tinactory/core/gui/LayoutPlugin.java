package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
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
}
