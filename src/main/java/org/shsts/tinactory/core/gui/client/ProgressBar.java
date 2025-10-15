package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ProgressBar extends MenuWidget {
    private final Texture texture;
    private final Texture texture2;
    private final String syncName;

    public enum Direction {
        HORIZONTAL, VERTICAL
    }

    public Direction direction = Direction.HORIZONTAL;

    public ProgressBar(MenuBase menu, Texture texture, Texture texture2, String syncName) {
        super(menu);
        this.texture = texture;
        this.texture2 = texture2;
        this.syncName = syncName;
    }

    public ProgressBar(MenuBase menu, Texture texture, String syncName) {
        this(menu, texture, texture, syncName);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var progress = MathUtil.clamp(menu.getSyncPacket(syncName, SyncPackets.DoublePacket.class)
            .map(SyncPackets.DoublePacket::getData).orElse(0d), 0d, 1d);
        var z = getBlitOffset();
        var h = rect.height();

        if (direction == Direction.HORIZONTAL) {
            var w1 = (int) (progress * (double) rect.width());
            var uh = texture == texture2 ? h : 0;
            RenderUtil.blit(poseStack, texture, z, rect);
            RenderUtil.blit(poseStack, texture2, z, rect.resize(w1, h), new Rect(0, uh, w1, h));
        } else {
            var w = rect.width();
            var h1 = (int) ((double) rect.height() * progress);
            var h2 = rect.height() - h1;
            var uh = texture == texture2 ? h : 0;
            RenderUtil.blit(poseStack, texture, z, rect);
            RenderUtil.blit(poseStack, texture2, z, rect.resize(w, h1).offset(0, h2), new Rect(0, uh + h2, w, h1));
        }
    }
}
