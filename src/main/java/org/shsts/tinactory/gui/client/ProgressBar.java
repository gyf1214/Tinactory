package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.Rect;
import org.shsts.tinactory.gui.Texture;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ProgressBar extends ContainerWidget {
    private final Texture texture;
    private final int dataIndex;

    public ProgressBar(ContainerMenu<?> menu, Rect rect, Texture texture, int dataIndex) {
        super(menu, rect, 20);
        this.texture = texture;
        this.dataIndex = dataIndex;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var progress = Math.max(0, (double) this.menu.getSimpleData(this.dataIndex) / (double) Short.MAX_VALUE);
        int w1 = (int) (progress * (double) this.rect.width());
        int w2 = this.rect.width() - w1;
        int h = this.rect.height();
        RenderUtil.blit(poseStack, this.texture, this.zIndex,
                this.rect.resize(w1, h), new Rect(0, h, w1, h));
        RenderUtil.blit(poseStack, this.texture, this.zIndex,
                this.rect.resize(w2, h).offset(w1, 0), new Rect(w1, 0, w2, h));
    }
}
