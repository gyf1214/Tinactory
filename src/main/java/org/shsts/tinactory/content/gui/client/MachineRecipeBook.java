package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.ContainerMenu.WIDTH;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends Panel {
    private static final Texture RECIPE_BOOK_BUTTON = new Texture(
            ModelGen.mcLoc("gui/recipe_button"), 256, 256);
    private static final Texture RECIPE_BOOK_BACKGROUND = new Texture(
            ModelGen.mcLoc("gui/recipe_book"), 256, 256);

    private static final int PANEL_WIDTH = WIDTH / 2;
    private static final int PANEL_BORDER = 8;
    private static final Rect BACKGROUND_TEX_RECT = new Rect(1, 1, 147, 166);


    private class RecipeBookButton extends Button {
        public RecipeBookButton(ContainerMenu<?> menu, Rect rect) {
            super(menu, rect, new TranslatableComponent("tinactory.tooltip.openRecipeBook"));
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var tex = RECIPE_BOOK_BUTTON;
            if (this.isHovering(mouseX, mouseY)) {
                RenderUtil.blit(poseStack, tex, this.zIndex, this.rect, 0, 19);
            } else {
                RenderUtil.blit(poseStack, tex, this.zIndex, this.rect);
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            bookPanel.visible = !bookPanel.visible;
        }
    }

    private final Panel bookPanel;

    public MachineRecipeBook(ContainerMenu<?> menu, int buttonX, int buttonY) {
        super(menu);
        this.bookPanel = new Panel(menu, RectD.corners(0d, 0d, 0d, 1d),
                Rect.corners(-MARGIN_HORIZONTAL - PANEL_WIDTH, -MARGIN_TOP, -MARGIN_HORIZONTAL, 0));
        this.bookPanel.addWidget(new StretchImage(menu, RectD.FULL, Rect.ZERO,
                RECIPE_BOOK_BACKGROUND, BACKGROUND_TEX_RECT, PANEL_BORDER));
        this.bookPanel.visible = false;

        this.addWidget(this.bookPanel);
        this.addWidget(new RecipeBookButton(menu, new Rect(buttonX, buttonY, 20, 18)));
    }
}
