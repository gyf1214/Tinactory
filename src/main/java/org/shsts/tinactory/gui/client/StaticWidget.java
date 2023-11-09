package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.layout.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class StaticWidget extends ContainerWidget {
    private final Texture texture;
    private final int zIndex;

    public StaticWidget(ContainerMenu<?> menu, Rect rect, Texture texture, int zIndex) {
        super(menu, rect, zIndex);
        this.texture = texture;
        this.zIndex = zIndex;
    }

    public StaticWidget(ContainerMenu<?> menu, Texture texture, int zIndex, int x, int y) {
        this(menu, new Rect(x, y, texture.width(), texture.height()), texture, zIndex);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.blit(poseStack, texture, zIndex, this.rect);
    }
}
