package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.client.MenuScreenBase;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreen1 extends MenuScreenBase implements IMenuScreen, IWidgetConsumer {
    protected final Panel rootPanel;
    protected final List<GuiComponent> hoverables = new ArrayList<>();

    public MenuScreen1(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.rootPanel = new Panel(this);
        for (var slot : menu.getMenu().slots) {
            int x = slot.x - 1 - MARGIN_HORIZONTAL;
            int y = slot.y - 1 - MARGIN_TOP;
            var slotBg = new StaticWidget(menu, Texture.SLOT_BACKGROUND);
            rootPanel.addWidget(new Rect(x, y, SLOT_SIZE, SLOT_SIZE), slotBg);
        }
    }

    @Override
    public void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget) {
        rootPanel.addGuiComponent(anchor, offset, widget);
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> void addWidgetToScreen(T widget) {
        super.addWidgetToScreen(widget);
        hoverables.add((GuiComponent) widget);
    }
}
