package org.shsts.tinactory.content.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class WrenchOutlineRenderer {
    private static final Vector4f COLOR = new Vector4f(0f, 0f, 0f, 0.4f);

    private static void renderLine(VertexConsumer vb, PoseStack.Pose pose, Vector3f pos1, Vector3f pos2) {
        var norm = pos2.copy();
        norm.sub(pos1);
        norm.normalize();
        vb.vertex(pose.pose(), pos1.x(), pos1.y(), pos1.z())
                .color(COLOR.x(), COLOR.y(), COLOR.z(), COLOR.w())
                .normal(pose.normal(), -norm.x(), -norm.y(), -norm.z())
                .endVertex();
        vb.vertex(pose.pose(), pos2.x(), pos2.y(), pos2.z())
                .color(COLOR.x(), COLOR.y(), COLOR.z(), COLOR.w())
                .normal(pose.normal(), norm.x(), norm.y(), norm.z())
                .endVertex();
    }

    private static void renderFaceOutline(VertexConsumer vb, PoseStack.Pose pose,
                                          Direction face, Direction dirU, Direction dirV) {
        var center = MathUtil.mulVecf(face.step(), 0.5f);
        var u = dirU.step();
        var v = dirV.step();
        var radius = (float) UsableToolItem.WRENCH_RADIUS_NORM;
        renderLine(vb, pose, MathUtil.addVecf(center, MathUtil.mulVecf(u, -0.5f), MathUtil.mulVecf(v, -radius)),
                MathUtil.addVecf(center, MathUtil.mulVecf(u, 0.5f), MathUtil.mulVecf(v, -radius)));
        renderLine(vb, pose, MathUtil.addVecf(center, MathUtil.mulVecf(u, -0.5f), MathUtil.mulVecf(v, radius)),
                MathUtil.addVecf(center, MathUtil.mulVecf(u, 0.5f), MathUtil.mulVecf(v, radius)));
        renderLine(vb, pose, MathUtil.addVecf(center, MathUtil.mulVecf(v, -0.5f), MathUtil.mulVecf(u, -radius)),
                MathUtil.addVecf(center, MathUtil.mulVecf(v, 0.5f), MathUtil.mulVecf(u, -radius)));
        renderLine(vb, pose, MathUtil.addVecf(center, MathUtil.mulVecf(v, -0.5f), MathUtil.mulVecf(u, radius)),
                MathUtil.addVecf(center, MathUtil.mulVecf(v, 0.5f), MathUtil.mulVecf(u, radius)));
    }

    public static void renderOutlines(PoseStack ms, MultiBufferSource bs,
                                      Camera camera, BlockPos pos, Direction face) {
        ms.pushPose();
        var camPos = camera.getPosition();
        ms.translate((double) pos.getX() - camPos.x + 0.5,
                (double) pos.getY() - camPos.y + 0.5,
                (double) pos.getZ() - camPos.z + 0.5);
        var vb = bs.getBuffer(RenderType.LINES);
        var pose = ms.last();
        switch (face.getAxis()) {
            case X -> renderFaceOutline(vb, pose, face, Direction.NORTH, Direction.UP);
            case Y -> renderFaceOutline(vb, pose, face, Direction.NORTH, Direction.EAST);
            case Z -> renderFaceOutline(vb, pose, face, Direction.EAST, Direction.UP);
        }
        ms.popPose();
    }
}
