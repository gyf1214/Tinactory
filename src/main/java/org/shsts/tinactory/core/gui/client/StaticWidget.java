package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StaticWidget extends ContainerWidget {
    private final Texture texture;

    public StaticWidget(ContainerMenu<?> menu, Rect rect, Texture texture) {
        super(menu, rect);
        this.texture = texture;
    }

    public StaticWidget(ContainerMenu<?> menu, Texture texture, int x, int y) {
        this(menu, new Rect(x, y, texture.width(), texture.height()), texture);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.blit(poseStack, texture, zIndex, rect);
    }
}
