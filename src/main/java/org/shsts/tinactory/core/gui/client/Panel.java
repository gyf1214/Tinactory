package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.common.ISelf;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends GuiComponent implements MenuBuilder.WidgetConsumer, ISelf<Panel> {
    protected record Child(RectD anchor, Rect offset, GuiComponent child) {
        public void setRect(Rect parent) {
            var sx = parent.inX(anchor.x()) + offset.x();
            var tx = parent.inX(anchor.endX()) + offset.endX();
            var sy = parent.inY(anchor.y()) + offset.y();
            var ty = parent.inY(anchor.endY()) + offset.endY();

            if (child instanceof MenuWidget menuWidget) {
                menuWidget.setRect(Rect.corners(sx, sy, tx, ty));
            } else if (child instanceof AbstractWidget widget) {
                widget.x = sx;
                widget.y = sy;
                widget.setWidth(tx - sx);
                widget.setHeight(ty - sy);
            } else if (child instanceof Panel panel) {
                panel.setRect(Rect.corners(sx, sy, tx, ty));
            }
        }

        public void setActive(boolean active) {
            if (child instanceof MenuWidget menuWidget) {
                menuWidget.setActive(active);
            } else if (child instanceof AbstractWidget widget) {
                widget.active = active;
                widget.visible = active;
            } else if (child instanceof Panel panel) {
                panel.setActive(false);
            }
        }

        public void addToScreen(MenuScreen<?> screen) {
            if (child instanceof MenuWidget widget) {
                screen.addWidgetToScreen(widget);
            } else if (child instanceof AbstractWidget widget) {
                screen.addWidgetToScreen(widget);
            } else if (child instanceof Panel panel) {
                panel.addToScreen();
            }
        }
    }

    protected final Menu<?> menu;
    protected final MenuScreen<?> screen;
    protected final List<Child> children = new ArrayList<>();
    protected boolean active = true;

    public Panel(MenuScreen<?> screen) {
        this.screen = screen;
        this.menu = screen.getMenu();
    }

    @Override
    public void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget) {
        children.add(new Child(anchor, offset, widget));
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
    void addVanillaWidget(Rect offset, T widget) {
        addGuiComponent(RectD.ZERO, offset, widget);
    }

    public void init(Rect rect) {
        setRect(rect);
        addToScreen();
    }

    protected void addToScreen() {
        for (var child : children) {
            child.addToScreen(screen);
        }
    }

    protected void setRect(Rect rect) {
        for (var child : children) {
            child.setRect(rect);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean value) {
        active = value;
        for (var child : children) {
            child.setActive(value);
        }
    }
}
