package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.client.IViewNode;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IViewAdapter extends IViewNode {
    void attach(MenuScreen<?> screen);

    boolean canHover();

    boolean isHovered(double mouseX, double mouseY);

    void renderTooltip(MenuScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY);
}
