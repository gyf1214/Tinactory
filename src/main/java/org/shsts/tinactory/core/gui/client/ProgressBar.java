package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProgressBar extends MenuWidget {
    private final Texture texture;
    private final int syncIndex;

    public ProgressBar(Menu<?, ?> menu, Texture texture, int syncIndex) {
        super(menu);
        this.texture = texture;
        this.syncIndex = syncIndex;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var progress = Math.max(menu.getSyncPacket(syncIndex, MenuSyncPacket.Double.class)
                .map(MenuSyncPacket.Double::getData).orElse(0d), 0d);
        int w1 = (int) (progress * (double) rect.width());
        int w2 = rect.width() - w1;
        int h = rect.height();
        var z = getBlitOffset();
        RenderUtil.blit(poseStack, texture, z, rect.resize(w1, h), new Rect(0, h, w1, h));
        RenderUtil.blit(poseStack, texture, z, rect.resize(w2, h).offset(w1, 0), new Rect(w1, 0, w2, h));
    }
}
