package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.shsts.tinactory.core.gui.Rect;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VanillaWidgetAdapter<T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
    implements IViewAdapter {
    private final T widget;

    VanillaWidgetAdapter(T widget) {
        this.widget = widget;
    }

    @Override
    public void initView() {}

    @Override
    public void setRect(Rect rect) {
        if (widget instanceof AbstractWidget abstractWidget) {
            abstractWidget.x = rect.x();
            abstractWidget.y = rect.y();
            abstractWidget.setWidth(rect.width());
            abstractWidget.setHeight(rect.height());
        }
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
    public void renderTooltip(MenuScreen<?> screen, PoseStack poseStack,
        int mouseX, int mouseY) {}
}
