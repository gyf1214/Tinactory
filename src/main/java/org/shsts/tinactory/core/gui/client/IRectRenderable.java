package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.gui.client.IRenderable;
import org.shsts.tinactory.core.gui.Rect;

@OnlyIn(Dist.CLIENT)
@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRectRenderable extends IRenderable {
    void render(PoseStack poseStack, Rect rect, int z);

    @Override
    default void render(PoseStack poseStack, int x, int y, int width, int height, int z) {
        render(poseStack, new Rect(x, y, width, height), z);
    }
}
