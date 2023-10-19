package org.shsts.tinactory.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ContainerMenuScreen<M extends ContainerMenu<?>> extends AbstractContainerScreen<M> {
    public static final int TEXT_COLOR = 4210752;
    public static final Texture BACKGROUND = new Texture(
            ModelGen.vendorLoc("gregtech", "gui/base/background"), ContainerMenu.WIDTH, 166);
    public static final int BG_Z_INDEX = 0;
    public static final Texture SLOT_BACKGROUND = new Texture(
            ModelGen.vendorLoc("gregtech", "gui/base/slot"), ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE);
    public static final int SLOT_BG_Z_INDEX = 20;

    protected final List<ContainerWidget.Factory> widgetsFactory = new ArrayList<>();

    public ContainerMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = ContainerMenu.MARGIN_HORIZONTAL;
        this.titleLabelY = ContainerMenu.MARGIN_VERTICAL;
        this.imageWidth = ContainerMenu.WIDTH;
        this.imageHeight = menu.getHeight();
    }

    public void addWidgetFactories(List<ContainerWidget.Factory> factories) {
        this.widgetsFactory.addAll(factories);
    }

    protected void addSlotWidget(Slot slot) {
        var x = slot.x + this.leftPos - 1;
        var y = slot.y + this.topPos - 1;
        this.renderables.add(new StaticWidget(SLOT_BACKGROUND, SLOT_BG_Z_INDEX, x, y));
    }

    @Override
    protected void init() {
        super.init();
        for (var slot : this.menu.slots) {
            this.addSlotWidget(slot);
        }
        for (var factory : this.widgetsFactory) {
            var widget = factory.factory().apply(factory.rect().offset(this.leftPos, this.topPos));
            this.renderables.add(widget);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        var x = this.leftPos;
        var y = this.topPos;
        var w = ContainerMenu.WIDTH;
        var h1 = ContainerMenu.MARGIN_VERTICAL;
        var h2 = this.imageHeight - 2 * h1;
        var h3 = BACKGROUND.height() - 2 * h1;
        RenderUtil.blit(poseStack, BACKGROUND, BG_Z_INDEX, new Rect(x, y, w, h1));
        RenderUtil.blit(poseStack, BACKGROUND, BG_Z_INDEX, new Rect(x, y + h1, w, h2), new Rect(0, h1, w, h3));
        RenderUtil.blit(poseStack, BACKGROUND, BG_Z_INDEX, new Rect(x, y + h1 + h2, w, h1), 0, h1 + h3);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float) this.titleLabelX, (float) this.titleLabelY, TEXT_COLOR);
    }
}
