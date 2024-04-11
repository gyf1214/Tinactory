package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.WIDTH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreen<M extends Menu<?>> extends AbstractContainerScreen<M> {
    public static final int TEXT_COLOR = 0xFF404040;

    protected final Panel rootPanel;
    protected final List<GuiComponent> hoverables = new ArrayList<>();

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

    public <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
    void addWidgetToScreen(T widget) {
        addRenderableWidget(widget);
        hoverables.add(widget);
    }

    public void initWidget(MenuBuilder.WidgetFactory<M> widget) {
        widget.accept(this, rootPanel);
    }

    @Override
    protected void init() {
        super.init();
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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var x = leftPos;
        var y = topPos;
        var w = WIDTH;
        var h1 = MARGIN_VERTICAL;
        var h2 = imageHeight - 2 * h1;
        var h3 = Texture.BACKGROUND.height() - 2 * h1;
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y, w, h1));
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y + h1, w, h2), new Rect(0, h1, w, h3));
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y + h1 + h2, w, h1), 0, h1 + h3);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        font.draw(poseStack, title, (float) titleLabelX, (float) titleLabelY, TEXT_COLOR);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            return;
        }
        for (var hoverable : hoverables) {
            if (hoverable instanceof MenuWidget widget && widget.isHovering(mouseX, mouseY)) {
                widget.getTooltip().ifPresent(tooltip ->
                        renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY));
                return;
            } else if (hoverable instanceof AbstractWidget widget && widget.isHoveredOrFocused()) {
                widget.renderToolTip(poseStack, mouseX, mouseY);
                return;
            }
        }
    }
}
