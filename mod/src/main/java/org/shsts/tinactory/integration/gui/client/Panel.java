package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.IViewGroup;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.core.gui.client.ViewGroup;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends GuiComponent implements IViewAdapter, IViewGroup {
    private Rect rect;

    protected record Child(int zIndex, Object source, IViewNode child) {
        public void attachToScreen(MenuScreen<?> screen) {
            if (child instanceof IViewAdapter adapter) {
                adapter.attach(screen);
            }
        }
    }

    private static final class WidgetAdapter<T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
        implements IViewAdapter {

        private final T widget;

        private WidgetAdapter(T widget) {
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

    protected final MenuBase menu;
    protected final MenuScreen<?> screen;
    protected final ViewGroup viewGroup;
    protected final List<Child> children = new ArrayList<>();
    protected boolean active = true;

    public Panel(MenuScreen<?> screen) {
        this(screen, new ViewGroup());
    }

    protected Panel(MenuScreen<?> screen, ViewGroup viewGroup) {
        this.screen = screen;
        this.menu = screen.menu();
        this.viewGroup = viewGroup;
    }

    public void init(Rect rect) {
        initView();
        setRect(rect);
        attach(screen);
    }

    public void refresh() {
        if (isActive()) {
            doRefresh();
        }
    }

    protected void doRefresh() {}

    @Override
    public void initView() {
        initPanel();
        children.sort(Comparator.comparing(Child::zIndex));
        viewGroup.initView();
    }

    protected void initPanel() {}

    @Override
    public void setRect(Rect rect) {
        this.rect = rect;
        viewGroup.setRect(rect);
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean value) {
        active = value;
        viewGroup.setActive(value);
        refresh();
    }

    @Override
    public void attach(MenuScreen<?> screen) {
        for (var child : children) {
            child.attachToScreen(screen);
        }
    }

    @Override
    public boolean canHover() {
        return false;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return mouseIn(mouseX, mouseY);
    }

    @Override
    public void renderTooltip(MenuScreen<?> screen, PoseStack poseStack,
        int mouseX, int mouseY) {}

    @Override
    public void addChild(RectD anchor, Rect offset, int zIndex, IViewNode child) {
        viewGroup.addChild(anchor, offset, zIndex, child);
        children.add(new Child(zIndex, child, child));
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        RectD anchor, Rect offset, int zIndex, T widget) {
        var adapter = new WidgetAdapter<>(widget);
        viewGroup.addChild(anchor, offset, zIndex, adapter);
        children.add(new Child(zIndex, widget, adapter));
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        RectD anchor, Rect offset, T widget) {
        addWidget(anchor, offset, 0, widget);
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(
        Rect offset, T widget) {
        addWidget(RectD.ZERO, offset, widget);
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> void addWidget(T widget) {
        addWidget(Rect.ZERO, widget);
    }

    public void addPanel(RectD anchor, Rect offset, int zIndex, IViewGroup panel) {
        addChild(anchor, offset, zIndex, panel);
    }

    public void addPanel(RectD anchor, Rect offset, IViewGroup panel) {
        addChild(anchor, offset, panel);
    }

    public void addPanel(Rect offset, IViewGroup panel) {
        addChild(RectD.FULL, offset, panel);
    }

    public void addPanel(IViewGroup panel) {
        addPanel(Rect.ZERO, panel);
    }

    protected void removeChild(Object source) {
        for (var i = 0; i < children.size(); i++) {
            var child = children.get(i);
            if (child.source() == source) {
                viewGroup.removeChild(child.child());
                children.remove(i);
                return;
            }
        }
    }

    public boolean mouseIn(double mouseX, double mouseY) {
        return rect.in(mouseX, mouseY);
    }
}
