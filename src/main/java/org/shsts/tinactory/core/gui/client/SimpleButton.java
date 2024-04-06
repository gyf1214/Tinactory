package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleButton extends Button {
    private final Texture texture;
    private final int normalX, normalY;
    private final int hoverX, hoverY;

    public SimpleButton(Menu<?> menu, RectD anchor, Rect offset, Texture texture,
                        @Nullable Component tooltip, int normalX, int normalY,
                        int hoverX, int hoverY) {
        super(menu, anchor, offset, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(Menu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int normalX, int normalY,
                        int hoverX, int hoverY) {
        super(menu, rect, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(Menu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int hoverX, int hoverY) {
        this(menu, rect, texture, tooltip, 0, 0, hoverX, hoverY);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (isHovering(mouseX, mouseY)) {
            RenderUtil.blit(poseStack, texture, zIndex, rect, hoverX, hoverY);
        } else {
            RenderUtil.blit(poseStack, texture, zIndex, rect, normalX, normalY);
        }
    }
}
