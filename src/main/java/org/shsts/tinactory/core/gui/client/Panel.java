package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends GuiComponent implements IWidgetConsumer {
    private Rect rect;

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
                panel.setActive(active);
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

        public void initPanel() {
            if (child instanceof Panel panel) {
                panel.initPanel();
            }
        }
    }

    protected final MenuBase menu;
    protected final MenuScreen<?> screen;
    protected final List<Child> children = new ArrayList<>();
    protected boolean active = true;

    public Panel(MenuScreen<?> screen) {
        this.screen = screen;
        this.menu = screen.menu();
    }

    @Override
    public void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget) {
        children.add(new Child(anchor, offset, widget));
    }

    public void init(Rect rect) {
        initPanel();
        setRect(rect);
        addToScreen();
    }

    /**
     * Override this for additional initialization.
     */
    protected void initPanel() {
        for (var child : children) {
            child.initPanel();
        }
    }

    public void refresh() {
        if (isActive()) {
            doRefresh();
        }
    }

    protected void doRefresh() {}

    protected void addToScreen() {
        for (var child : children) {
            child.addToScreen(screen);
        }
    }

    protected void setRect(Rect rect) {
        this.rect = rect;
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
        refresh();
    }

    public boolean mouseIn(double mouseX, double mouseY) {
        return rect.in(mouseX, mouseY);
    }
}
