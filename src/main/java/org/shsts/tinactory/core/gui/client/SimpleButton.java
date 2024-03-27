package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleButton extends Button {
    private final Texture texture;
    private final int normalX, normalY;
    private final int hoverX, hoverY;

    public SimpleButton(ContainerMenu<?> menu, RectD anchor, Rect offset, Texture texture,
                        @Nullable Component tooltip, int normalX, int normalY,
                        int hoverX, int hoverY) {
        super(menu, anchor, offset, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(ContainerMenu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int normalX, int normalY,
                        int hoverX, int hoverY) {
        super(menu, rect, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(ContainerMenu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int hoverX, int hoverY) {
        this(menu, rect, texture, tooltip, 0, 0, hoverX, hoverY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.isHovering(mouseX, mouseY)) {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect, this.hoverX, this.hoverY);
        } else {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect, this.normalX, this.normalY);
        }
    }

    @Override
    public abstract void onMouseClicked(double mouseX, double mouseY, int button);
}
