package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.MenuBase;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StaticWidget extends MenuWidget {
    private final Texture texture;
    private final int srcX;
    private final int srcY;

    public StaticWidget(MenuBase menu, Texture texture, int srcX, int srcY) {
        super(menu);
        this.texture = texture;
        this.srcX = srcX;
        this.srcY = srcY;
    }

    public StaticWidget(MenuBase menu, Texture texture) {
        this(menu, texture, 0, 0);
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        RenderUtil.blit(graphics, texture, rect(), srcX, srcY);
    }
}
