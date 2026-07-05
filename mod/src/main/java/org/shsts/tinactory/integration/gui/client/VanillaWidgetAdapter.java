package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.shsts.tinactory.core.gui.Rect;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VanillaWidgetAdapter<T extends GuiEventListener & Renderable & NarratableEntry>
    implements IViewAdapter {
    private final T widget;

    public VanillaWidgetAdapter(T widget) {
        this.widget = widget;
    }

    @Override
    public void setRect(Rect rect) {
        if (widget instanceof AbstractWidget abstractWidget) {
            abstractWidget.setRectangle(rect.x(), rect.y(), rect.width(), rect.height());
        }
    }

    @Override
    public Rect rect() {
        var rect1 = widget.getRectangle();
        return new Rect(rect1.left(), rect1.top(), rect1.width(), rect1.height());
    }

    @Override
    public void setActive(boolean active) {
        if (widget instanceof AbstractWidget abstractWidget) {
            abstractWidget.active = active;
            abstractWidget.visible = active;
        }
    }

    @Override
    public void attach(MenuScreen<?> screen) {
        screen.addWidgetToScreen(widget);
    }

    @Override
    public boolean canHover() {
        return false;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void renderTooltip(MenuScreen<?> screen, GuiGraphics graphics,
        int mouseX, int mouseY) {}
}
