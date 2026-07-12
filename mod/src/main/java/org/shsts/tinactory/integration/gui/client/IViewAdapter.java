package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.client.IViewNode;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IViewAdapter extends IViewNode {
    void attach(MenuScreen<?> screen);

    boolean canHover();

    boolean isHovered(double mouseX, double mouseY);

    void renderTooltip(MenuScreen<?> screen, GuiGraphics graphics, int mouseX, int mouseY);
}
