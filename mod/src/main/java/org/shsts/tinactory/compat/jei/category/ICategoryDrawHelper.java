package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICategoryDrawHelper {
    void drawProgressBar(GuiGraphics graphics, int cycle);
}
