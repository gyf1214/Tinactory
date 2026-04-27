package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
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

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends GuiComponent implements IViewAdapter, IViewGroup {
    private Rect rect;

    protected final MenuBase menu;
    protected final MenuScreen<?> screen;
    protected final ViewGroup viewGroup;
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
    public void renderTooltip(MenuScreen<?> screen, PoseStack poseStack,
        int mouseX, int mouseY) {}

    @Override
    public void forEachChild(Consumer<IViewNode> consumer) {
        viewGroup.forEachChild(consumer);
    }

    @Override
    public void addChild(RectD anchor, Rect offset, int zIndex, IViewNode child) {
        viewGroup.addChild(anchor, offset, zIndex, child);
    }

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry> IViewNode addVanillaWidget(
        RectD anchor, Rect offset, int zIndex, T widget) {
        var adapter = new VanillaWidgetAdapter<>(widget);
        viewGroup.addChild(anchor, offset, zIndex, adapter);
        return adapter;
    }

    protected void removeChild(IViewNode child) {
        viewGroup.removeChild(child);
    }

    public boolean mouseIn(double mouseX, double mouseY) {
        return rect.in(mouseX, mouseY);
    }
}
