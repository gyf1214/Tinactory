package org.shsts.tinactory.core.tool;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraftforge.common.util.Lazy;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UsableToolItem extends ToolItem {
    public static final int WRENCH_RADIUS = 4;
    public static final double WRENCH_RADIUS_NORM = (double) WRENCH_RADIUS / 16d;
    private final Tier tier;
    private final TagKey<Block> blockTag;
    @Nullable
    private final Supplier<SoundEvent> sound;

    public UsableToolItem(Properties properties, int durability,
        Tier tier, TagKey<Block> blockTag, @Nullable Supplier<SoundEvent> sound) {
        super(properties, durability);
        this.tier = tier;
        this.blockTag = blockTag;
        this.sound = sound == null ? null : Lazy.of(sound);
    }

    public Tier tier() {
        return tier;
    }

    private static Direction wrenchedDirection(BlockPos pos, Direction clickFace, Vec3 clickLoc) {
        var faceCenter = MathUtil.blockCenter(pos)
            .add(MathUtil.dirNormal(clickFace).scale(0.5));
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
        var player = ctx.getPlayer();
        if (player != null && ctx.getHand() == InteractionHand.MAIN_HAND) {
            var pos = ctx.getClickedPos();
            var state = ctx.getLevel().getBlockState(pos);
            var tool = ctx.getItemInHand();

            if (state.getBlock() instanceof IWrenchable wrenchable &&
                wrenchable.canWrenchWith(tool)) {
                var sneaking = player.isShiftKeyDown();
                var dir = wrenchedDirection(pos, ctx.getClickedFace(), ctx.getClickLocation());

                wrenchable.onWrenchWith(ctx.getLevel(), pos, state, tool, dir, sneaking);
                doDamage(stack, 1, player, ctx.getHand());
                if (sound != null) {
                    player.playSound(sound.get(), 1f, 1f);
                }

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(blockTag) ? tier.getSpeed() : 1.0F;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (state.getDestroySpeed(world, pos) != 0f) {
            if (!world.isClientSide) {
                doDamage(stack, 1, entity, InteractionHand.MAIN_HAND);
                // mineBlock is only called on Server, so we need to broadcast this to all players.
                if (sound != null) {
                    world.playSound(null, entity, sound.get(), entity.getSoundSource(), 1f, 1f);
                }
            }
        }
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(blockTag) && TierSortingRegistry.isCorrectTierForDrops(tier, state);
    }
}
