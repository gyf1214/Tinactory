package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleButton extends Button {
    protected final Texture texture;
    protected final int normalX, normalY;
    protected final int hoverX, hoverY;

    public SimpleButton(Menu<?, ?> menu, Texture texture,
                        @Nullable Component tooltip, int normalX, int normalY,
                        int hoverX, int hoverY) {
        super(menu, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(Menu<?, ?> menu, Texture texture,
                        @Nullable Component tooltip, int hoverX, int hoverY) {
        this(menu, texture, tooltip, 0, 0, hoverX, hoverY);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (isHovering(mouseX, mouseY)) {
            RenderUtil.blit(poseStack, texture, getBlitOffset(), rect, hoverX, hoverY);
        } else {
            RenderUtil.blit(poseStack, texture, getBlitOffset(), rect, normalX, normalY);
        }
    }
}
