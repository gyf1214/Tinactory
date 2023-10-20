package org.shsts.tinactory.content.tool;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.shsts.tinactory.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolItem extends Item implements IToolItem {
    public static final int WRENCH_RADIUS = 4;
    public static final double WRENCH_RADIUS_NORM = (double) WRENCH_RADIUS / 16d;

    protected final int level;
    protected final int maxDurability;

    public ToolItem(Properties properties, int level, int maxDurability) {
        super(properties);
        this.level = level;
        this.maxDurability = maxDurability;
    }

    public int getDurability(ItemStack stack) {
        var tag = stack.getTag();
        return tag == null || !tag.contains("durability", Tag.TAG_INT) ? maxDurability :
                Math.min(maxDurability, tag.getInt("durability"));
    }

    public void setDurability(ItemStack stack, int value) {
        var tag = stack.getOrCreateTag();
        tag.putInt("durability", value);
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public boolean canDamage(ItemStack stack, int damage) {
        return true;
    }

    @Override
    public ItemStack doDamage(ItemStack stack, int damage) {
        var newDurability = getDurability(stack) - damage;
        if (newDurability <= 0) {
            return ItemStack.EMPTY;
        }
        setDurability(stack, newDurability);
        return stack;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * (float) getDurability(stack) / (float) maxDurability);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb((float) getDurability(stack) / (float) maxDurability / 3f, 1f, 1f);
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
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
