package org.shsts.tinactory.content.tool;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.shsts.tinactory.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrenchItem extends Item {
    public static final int RADIUS = 4;
    public static final double RADIUS_NORM = (double) RADIUS / 16d;

    public WrenchItem(Properties properties) {
        super(properties);
    }

    private static Direction wrenchedDirection(BlockPos pos, Direction clickFace, Vec3 clickLoc) {
        var faceCenter = MathUtil.blockCenter(pos)
                .add(MathUtil.dirNormal(clickFace).scale(0.5d));
        var clickRel = clickLoc.subtract(faceCenter);
        var dx = MathUtil.compare(clickRel.x, RADIUS_NORM);
        var dy = MathUtil.compare(clickRel.y, RADIUS_NORM);
        var dz = MathUtil.compare(clickRel.z, RADIUS_NORM);
        var delta = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
        if (delta == 0) {
            return clickFace;
        } else if (delta >= 2) {
            return clickFace.getOpposite();
        }
        if (dx != 0) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (dy != 0) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
        if (ctx.getHand() == InteractionHand.MAIN_HAND) {
            var pos = ctx.getClickedPos();
            var state = ctx.getLevel().getBlockState(pos);
            var tool = ctx.getItemInHand();

            if (state.getBlock() instanceof IWrenchable wrenchable) {
                var sneaking = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
                var dir = wrenchedDirection(pos, ctx.getClickedFace(), ctx.getClickLocation());
                wrenchable.onWrenchWith(ctx.getLevel(), pos, state, tool, dir, sneaking);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
