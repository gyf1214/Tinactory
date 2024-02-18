package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class ProgressBar extends ContainerWidget {
    private final Texture texture;
    private final int syncIndex;

    public ProgressBar(ContainerMenu<?> menu, Rect rect, Texture texture, int syncIndex) {
        super(menu, rect, ContainerMenu.DEFAULT_Z_INDEX);
        this.texture = texture;
        this.syncIndex = syncIndex;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var progress = Math.max(this.menu.getSyncPacket(this.syncIndex, ContainerSyncPacket.Double.class)
                .map(ContainerSyncPacket.Double::getData).orElse(0d), 0d);
        int w1 = (int) (progress * (double) this.rect.width());
        int w2 = this.rect.width() - w1;
        int h = this.rect.height();
        RenderUtil.blit(poseStack, this.texture, this.zIndex,
                this.rect.resize(w1, h), new Rect(0, h, w1, h));
        RenderUtil.blit(poseStack, this.texture, this.zIndex,
                this.rect.resize(w2, h).offset(w1, 0), new Rect(w1, 0, w2, h));
    }
}
