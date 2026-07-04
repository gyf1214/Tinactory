package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.MenuBase;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SimpleButton extends Button {
    protected final Texture texture;
    protected final int normalX, normalY;
    protected final int hoverX, hoverY;

    public SimpleButton(MenuBase menu, Texture texture,
        @Nullable Component tooltip, int normalX, int normalY,
        int hoverX, int hoverY) {
        super(menu, tooltip);
        this.texture = texture;
        this.normalX = normalX;
        this.normalY = normalY;
        this.hoverX = hoverX;
        this.hoverY = hoverY;
    }

    public SimpleButton(MenuBase menu, Texture texture,
        @Nullable Component tooltip, int hoverX, int hoverY) {
        this(menu, texture, tooltip, 0, 0, hoverX, hoverY);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var rect = requireRect();
        if (isHovered(mouseX, mouseY)) {
            RenderUtil.blit(graphics, texture, rect, hoverX, hoverY);
        } else {
            RenderUtil.blit(graphics, texture, rect, normalX, normalY);
        }
    }
}
