package org.shsts.tinactory.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StaticWidget extends ContainerWidget {
    private final Texture texture;
    private final int zIndex;

    public StaticWidget(Rect rect, Texture texture, int zIndex) {
        super(rect);
        this.texture = texture;
        this.zIndex = zIndex;
    }

    public StaticWidget(Texture texture, int zIndex, int x, int y) {
        this(new Rect(x, y, texture.width(), texture.height()), texture, zIndex);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.blit(poseStack, texture, zIndex, this.rect);
    }
}
