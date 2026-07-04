package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.IViewGroup;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.core.gui.client.ViewGroup;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel implements IViewAdapter, IViewGroup {
    @Nullable
    private Rect rect;

    protected final MenuBase menu;
    protected final MenuScreen<?> screen;
    protected final ViewGroup viewGroup;
    protected boolean active = true;
    protected boolean laidOut = false;
    protected boolean refreshPending = false;

    public Panel(MenuScreen<?> screen) {
        this(screen, new ViewGroup());
    }

    protected Panel(MenuScreen<?> screen, ViewGroup viewGroup) {
        this.screen = screen;
        this.menu = screen.menu();
        this.viewGroup = viewGroup;
    }

    public void init(Rect rect) {
        setRect(rect);
        attach(screen);
    }

    public void refresh() {
        if (!laidOut) {
            refreshPending = true;
            return;
        }
        if (!isActive()) {
            refreshPending = true;
            return;
        }
        refreshPending = false;
        doRefresh();
    }

    protected void doRefresh() {}

    @Override
    public void setRect(Rect rect) {
        laidOut = false;
        this.rect = rect;
        viewGroup.setRect(rect);
        laidOut = true;
        postLayout();
    }

    protected void postLayout() {
        if (refreshPending && isActive()) {
            refresh();
        }
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean value) {
        active = value;
        viewGroup.setActive(value);
        if (value && laidOut) {
            refresh();
        }
    }

    @Override
    public void attach(MenuScreen<?> screen) {
        forEachChild(child -> {
            if (child instanceof IViewAdapter adapter) {
                adapter.attach(screen);
            }
        });
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
    public void renderTooltip(MenuScreen<?> screen, GuiGraphics graphics,
        int mouseX, int mouseY) {}

    @Override
    public void forEachChild(Consumer<IViewNode> consumer) {
        viewGroup.forEachChild(consumer);
    }

    @Override
    public void addChild(RectD anchor, Rect offset, int zIndex, IViewNode child) {
        viewGroup.addChild(anchor, offset, zIndex, child);
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> IViewNode addVanillaWidget(
        RectD anchor, Rect offset, int zIndex, T widget) {
        var adapter = new VanillaWidgetAdapter<>(widget);
        viewGroup.addChild(anchor, offset, zIndex, adapter);
        return adapter;
    }

    protected void removeChild(IViewNode child) {
        viewGroup.removeChild(child);
    }

    public boolean mouseIn(double mouseX, double mouseY) {
        assert rect != null : "Panel rect must be assigned before geometry reads";
        return rect.in(mouseX, mouseY);
    }
}
