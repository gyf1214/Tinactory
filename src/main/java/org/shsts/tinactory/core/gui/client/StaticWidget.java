package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.gui.IMenu;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StaticWidget extends MenuWidget {
    private final Texture texture;

    public StaticWidget(IMenu menu, Texture texture) {
        super(menu);
        this.texture = texture;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.blit(poseStack, texture, getBlitOffset(), rect);
    }
}
