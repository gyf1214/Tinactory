package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuWidget implements IViewAdapter, Renderable, GuiEventListener, NarratableEntry {

    protected final MenuBase menu;
    @Nullable
    protected Rect rect;
    protected boolean active = true;
    protected boolean focused = false;

    public MenuWidget(MenuBase menu) {
        this.menu = menu;
    }

    @Override
    public void setRect(Rect rect) {
        this.rect = rect;
    }

    @Override
    public void setActive(boolean value) {
        active = value;
        if (!value) {
            focused = false;
        }
    }

    protected Rect requireRect() {
        assert rect != null : "Widget rect must be assigned before geometry reads";
        return Objects.requireNonNull(rect, "Widget rect must be assigned before geometry reads");
    }

    public boolean isActive() {
        return active;
    }

    public abstract void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (active) {
            requireRect();
            doRender(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean canHover() {
        return false;
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return active && canHover() && requireRect().in(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return active && requireRect().in(mouseX, mouseY);
    }

    @Override
    public ScreenRectangle getRectangle() {
        var rect = requireRect();
        return new ScreenRectangle(rect.x(), rect.y(), rect.width(), rect.height());
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = active && focused;
    }

    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        return Optional.empty();
    }

    @Override
    public void attach(MenuScreen<?> screen) {
        screen.addWidgetToScreen(this);
        if (canHover()) {
            screen.addHoverable(this);
        }
    }

    @Override
    public void renderTooltip(MenuScreen<?> screen, GuiGraphics graphics, int mouseX, int mouseY) {
        if (!active) {
            return;
        }
        getTooltip(mouseX, mouseY).ifPresent(tooltip ->
            screen.renderTooltip(graphics, tooltip, Optional.empty(), mouseX, mouseY));
    }

    protected boolean canClick(int button, double mouseX, double mouseY) {
        return false;
    }

    protected boolean isClicking(double mouseX, double mouseY, int button) {
        return active && requireRect().in(mouseX, mouseY) && canClick(button, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isClicking(mouseX, mouseY, button)) {
            onMouseClicked(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    /**
     * Do not consider drag now.
     */
    public void onMouseClicked(double mouseX, double mouseY, int button) {}

    @Override
    public NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
}
