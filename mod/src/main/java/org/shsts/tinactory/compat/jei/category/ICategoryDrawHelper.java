package org.shsts.tinactory.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICategoryDrawHelper {
    void drawProgressBar(PoseStack stack, int cycle);

    IDrawable getBackground();
}
