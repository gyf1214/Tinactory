package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.util.MathUtil;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProgressBar extends MenuWidget {
    private final Texture texture;
    private final Texture texture2;
    private final int syncIndex;

    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    public Direction direction = Direction.HORIZONTAL;

    public ProgressBar(Menu<?, ?> menu, Texture texture, Texture texture2, int syncIndex) {
        super(menu);
        this.texture = texture;
        this.texture2 = texture2;
        this.syncIndex = syncIndex;
    }

    public ProgressBar(Menu<?, ?> menu, Texture texture, int syncIndex) {
        this(menu, texture, texture, syncIndex);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var progress = MathUtil.clamp(menu1.getSyncPacket(syncIndex, MenuSyncPacket.Double.class)
            .map(MenuSyncPacket.Double::getData).orElse(0d), 0d, 1d);
        var z = getBlitOffset();
        var h = rect.height();

        if (direction == Direction.HORIZONTAL) {
            var w1 = (int) (progress * (double) rect.width());
            var w2 = rect.width() - w1;
            var uh = texture == texture2 ? h : 0;
            RenderUtil.blit(poseStack, texture2, z, rect.resize(w1, h), new Rect(0, uh, w1, h));
            RenderUtil.blit(poseStack, texture, z, rect.resize(w2, h).offset(w1, 0), new Rect(w1, 0, w2, h));
        } else {
            var w = rect.width();
            var h1 = (int) ((double) rect.height() * progress);
            var h2 = rect.height() - h1;
            var uh = texture == texture2 ? h : 0;
            RenderUtil.blit(poseStack, texture2, z, rect.resize(w, h1).offset(0, h2), new Rect(0, uh + h2, w, h1));
            RenderUtil.blit(poseStack, texture, z, rect.resize(w, h2), new Rect(0, 0, w, h2));
        }
    }
}
