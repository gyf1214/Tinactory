package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuWidget extends GuiComponent implements
    IViewAdapter, Widget, GuiEventListener, NarratableEntry {

    protected final MenuBase menu;
    protected Rect rect;
    protected boolean active = true;

    public MenuWidget(MenuBase menu) {
        this.menu = menu;
        this.setBlitOffset(0);
    }

    @Override
    public void initView() {}

    @Override
    public void setRect(Rect rect) {
        this.rect = rect;
    }

    @Override
    public void setActive(boolean value) {
        active = value;
    }

    public abstract void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (active) {
            doRender(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean canHover() {
        return false;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return active && canHover() && rect.in(mouseX, mouseY);
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        return isHovering(mouseX, mouseY);
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
    public void renderTooltip(MenuScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        getTooltip(mouseX, mouseY).ifPresent(tooltip ->
            screen.renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY));
    }

    protected boolean canClick(int button, double mouseX, double mouseY) {
        return false;
    }

    protected boolean isClicking(double mouseX, double mouseY, int button) {
        return active && rect.in(mouseX, mouseY) && canClick(button, mouseX, mouseY);
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
