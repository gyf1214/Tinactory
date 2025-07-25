package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.client.MenuScreenBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.BACKGROUND;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreen extends MenuScreenBase implements IWidgetConsumer {
    protected final Panel rootPanel;
    protected final List<Widget> hoverables = new ArrayList<>();

    public int contentWidth;
    public int contentHeight;

    public MenuScreen(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = MARGIN_X;
        this.titleLabelY = MARGIN_VERTICAL;
        this.contentWidth = CONTENT_WIDTH;
        this.contentHeight = 0;

        this.rootPanel = new Panel(this);
        for (var slot : menu.getMenu().slots) {
            // TODO: need a more consistent way to determine whether the slot needs background
            if (slot.isActive()) {
                int x = slot.x - 1 - MARGIN_X;
                int y = slot.y - 1 - MARGIN_TOP;
                var slotBg = new StaticWidget(menu, SLOT_BACKGROUND);
                rootPanel.addWidget(new Rect(x, y, SLOT_SIZE, SLOT_SIZE), slotBg);
            }
        }
    }

    public IMenu menu() {
        return iMenu;
    }

    @Override
    public void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget) {
        rootPanel.addGuiComponent(anchor, offset, widget);
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> void addWidgetToScreen(
        T widget) {
        super.addWidgetToScreen(widget);
        hoverables.add(widget);
    }

    protected void centerWindow() {
        super.init();
    }

    @Override
    protected void init() {
        imageWidth = contentWidth + MARGIN_X * 2;
        imageHeight = contentHeight + MARGIN_TOP + MARGIN_VERTICAL;
        centerWindow();
        initRect();
    }

    protected void initRect() {
        var rect = new Rect(leftPos + MARGIN_X, topPos + MARGIN_TOP,
            imageWidth - MARGIN_X * 2, imageHeight - MARGIN_TOP - MARGIN_VERTICAL);
        rootPanel.init(rect);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        hoverables.clear();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var tex = BACKGROUND;
        var rect = new Rect(leftPos, topPos, imageWidth, imageHeight);
        var texRect = new Rect(0, 0, tex.width(), tex.height());
        StretchImage.render(poseStack, tex, 0, rect, texRect, MARGIN_VERTICAL);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, title, (float) titleLabelX, (float) titleLabelY, RenderUtil.TEXT_COLOR);
    }

    public Optional<Widget> getHovered(int mouseX, int mouseY) {
        for (var hoverable : hoverables) {
            if (hoverable instanceof MenuWidget widget && widget.isHovering(mouseX, mouseY)) {
                return Optional.of(hoverable);
            } else if (hoverable instanceof AbstractWidget widget && widget.isHoveredOrFocused()) {
                return Optional.of(hoverable);
            }
        }
        return Optional.empty();
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            return;
        }

        getHovered(mouseX, mouseY).ifPresent(hoverable -> {
            if (hoverable instanceof MenuWidget widget) {
                widget.getTooltip(mouseX, mouseY).ifPresent(tooltip ->
                    renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY));
            } else if (hoverable instanceof AbstractWidget widget) {
                widget.renderToolTip(poseStack, mouseX, mouseY);
            }
        });
    }
}
