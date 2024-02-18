package org.shsts.tinactory.content.tool;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.TierSortingRegistry;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UsableToolItem extends ToolItem {
    public static final int WRENCH_RADIUS = 4;
    public static final double WRENCH_RADIUS_NORM = (double) WRENCH_RADIUS / 16d;
    protected final Tier tier;
    protected final TagKey<Block> blockTag;

    public UsableToolItem(Properties properties, int durability, Tier tier, TagKey<Block> blockTag) {
        super(properties, durability);
        this.tier = tier;
        this.blockTag = blockTag;
    }

    private static Direction wrenchedDirection(BlockPos pos, Direction clickFace, Vec3 clickLoc) {
        var faceCenter = MathUtil.blockCenter(pos)
                .add(MathUtil.dirNormal(clickFace).scale(0.5d));
        var clickRel = clickLoc.subtract(faceCenter);
        var dx = MathUtil.compare(clickRel.x, WRENCH_RADIUS_NORM);
        var dy = MathUtil.compare(clickRel.y, WRENCH_RADIUS_NORM);
        var dz = MathUtil.compare(clickRel.z, WRENCH_RADIUS_NORM);
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

            if (state.getBlock() instanceof IWrenchable wrenchable &&
                    wrenchable.canWrenchWith(tool)) {
                var sneaking = ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown();
                var dir = wrenchedDirection(pos, ctx.getClickedFace(), ctx.getClickLocation());
                wrenchable.onWrenchWith(ctx.getLevel(), pos, state, tool, dir, sneaking);
                doDamage(stack, 1, ctx.getPlayer(), ctx.getHand());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return this.tier.getSpeed();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!world.isClientSide && state.getDestroySpeed(world, pos) != 0.0F) {
            doDamage(stack, 1, entity, InteractionHand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(this.blockTag) && TierSortingRegistry.isCorrectTierForDrops(this.tier, state);
    }
}
