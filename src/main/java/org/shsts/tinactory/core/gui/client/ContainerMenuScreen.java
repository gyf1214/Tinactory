package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.ContainerMenu.WIDTH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerMenuScreen<M extends ContainerMenu<?>> extends AbstractContainerScreen<M> {
    public static final int TEXT_COLOR = 0xFF404040;

    protected final Panel rootPanel;
    protected boolean isHovering = false;

    public ContainerMenuScreen(M menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = MARGIN_HORIZONTAL;
        this.titleLabelY = MARGIN_VERTICAL;
        this.imageWidth = WIDTH;
        this.imageHeight = menu.getHeight();

        this.rootPanel = new Panel(menu, RectD.FULL, Rect.corners(MARGIN_HORIZONTAL, MARGIN_TOP, 0, 0));
        for (var slot : this.menu.slots) {
            int x = slot.x - 1 - MARGIN_HORIZONTAL;
            int y = slot.y - 1 - MARGIN_TOP;
            var slotBg = new StaticWidget(this.menu, Texture.SLOT_BACKGROUND, x, y);
            this.rootPanel.addWidget(slotBg);
        }
    }

    public void addWidget(Function<M, ContainerWidget> factory) {
        var widget = factory.apply(this.menu);
        this.rootPanel.addWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        this.rootPanel.init(new Rect(this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
        this.renderables.add(this.rootPanel);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        this.isHovering = this.rootPanel.isHovering(mouseX, mouseY);
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
        if (this.menu.getCarried().isEmpty() && this.isHovering) {
            this.rootPanel.getTooltip().ifPresent(tooltip ->
                    this.renderTooltip(poseStack, tooltip, Optional.empty(), mouseX, mouseY));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.rootPanel.isClicking(mouseX, mouseY, button)) {
            this.rootPanel.onMouseClicked(mouseX, mouseY, button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
