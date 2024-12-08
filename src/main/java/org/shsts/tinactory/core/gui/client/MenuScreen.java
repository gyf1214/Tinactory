package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.WIDTH;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreen<M extends Menu<?, M>> extends AbstractContainerScreen<M>
    implements IMenuScreen, IWidgetConsumer {
    private static final Texture BACKGROUND = new Texture(gregtech("gui/base/background"), WIDTH, 166);

    protected final Panel rootPanel;
    protected final List<Widget> hoverables = new ArrayList<>();

    public MenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = MARGIN_HORIZONTAL;
        this.titleLabelY = MARGIN_VERTICAL;
        this.imageWidth = WIDTH;
        this.imageHeight = menu.getHeight();

        this.rootPanel = new Panel(this);
        for (var slot : menu.slots) {
            int x = slot.x - 1 - MARGIN_HORIZONTAL;
            int y = slot.y - 1 - MARGIN_TOP;
            var slotBg = new StaticWidget(menu, Texture.SLOT_BACKGROUND);
            rootPanel.addWidget(new Rect(x, y, SLOT_SIZE, SLOT_SIZE), slotBg);
        }
    }

    @Override
    public void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget) {
        rootPanel.addGuiComponent(anchor, offset, widget);
    }

    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> void addWidgetToScreen(
        T widget) {
        addRenderableWidget(widget);
        hoverables.add(widget);
    }

    @Override
    protected void init() {
        super.init();
        initRect();
    }

    protected void initRect() {
        var rect = new Rect(leftPos + MARGIN_HORIZONTAL, topPos + MARGIN_TOP,
            imageWidth - MARGIN_HORIZONTAL * 2, imageHeight - MARGIN_TOP - MARGIN_VERTICAL);
        rootPanel.init(rect);
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        hoverables.clear();
    }

    @Override
    public void removed() {
        super.removed();
        menu.removeScreenPlugin();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var tex = BACKGROUND;
        var rect = new Rect(leftPos, topPos, imageWidth, imageHeight);
        var texRect = new Rect(0, 0, tex.width(), tex.height());
        StretchImage.render(poseStack, BACKGROUND, 0, rect, texRect, MARGIN_VERTICAL);
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
