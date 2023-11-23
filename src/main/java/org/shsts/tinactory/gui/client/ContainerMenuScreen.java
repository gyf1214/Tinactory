package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.layout.Texture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.gui.ContainerMenu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.gui.ContainerMenu.MARGIN_TOP;
import static org.shsts.tinactory.gui.ContainerMenu.MARGIN_VERTICAL;
import static org.shsts.tinactory.gui.ContainerMenu.WIDTH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ContainerMenuScreen<M extends ContainerMenu<?>> extends AbstractContainerScreen<M> {
    public static final int TEXT_COLOR = 4210752;

    protected final List<ContainerWidget.Builder<M>> widgetBuilders = new ArrayList<>();
    protected final List<ContainerWidget> widgets = new ArrayList<>();
    protected @Nullable ContainerWidget hoveredWidget = null;

    public ContainerMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = MARGIN_HORIZONTAL;
        this.titleLabelY = MARGIN_VERTICAL;
        this.imageWidth = WIDTH;
        this.imageHeight = menu.getHeight();
    }

    public void addWidgetBuilder(ContainerWidget.Builder<M> factory) {
        this.widgetBuilders.add(factory);
    }

    protected void addWidget(ContainerWidget widget) {
        this.widgets.add(widget);
        this.renderables.add(widget);
    }

    protected void addSlotWidget(int x, int y) {
        var pX = x + this.leftPos - 1;
        var pY = y + this.topPos - 1;
        this.addWidget(new StaticWidget(this.menu, Texture.SLOT_BACKGROUND,
                ContainerMenu.DEFAULT_Z_INDEX, pX, pY));
    }

    @Override
    protected void init() {
        super.init();
        for (var slot : this.menu.slots) {
            this.addSlotWidget(slot.x, slot.y);
        }
        for (var builder : this.widgetBuilders) {
            var widget = builder.factory().apply(this.menu, builder.rect().offset(
                    MARGIN_HORIZONTAL + this.leftPos, MARGIN_TOP + this.topPos));
            this.addWidget(widget);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.hoveredWidget = null;
        for (var widget : this.widgets) {
            if (widget.isHovering(mouseX, mouseY)) {
                this.hoveredWidget = widget;
            }
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var x = this.leftPos;
        var y = this.topPos;
        var w = WIDTH;
        var h1 = MARGIN_VERTICAL;
        var h2 = this.imageHeight - 2 * h1;
        var h3 = Texture.BACKGROUND.height() - 2 * h1;
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y, w, h1));
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y + h1, w, h2), new Rect(0, h1, w, h3));
        RenderUtil.blit(poseStack, Texture.BACKGROUND, 0, new Rect(x, y + h1 + h2, w, h1), 0, h1 + h3);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, TEXT_COLOR);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        super.renderTooltip(poseStack, mouseX, mouseY);
        if (this.menu.getCarried().isEmpty() && this.hoveredWidget != null) {
            this.hoveredWidget.getTooltip().ifPresent(tooltip ->
                    this.renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY));
        }
    }
}
