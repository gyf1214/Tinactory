package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredChainsawItem extends PoweredToolItem {
    private final int maxSearchBlocks;

    public PoweredChainsawItem(Item.Properties properties, Config config, int maxSearchBlocks) {
        super(properties.component(DataComponents.TOOL, tool(config)), config);
        this.maxSearchBlocks = maxSearchBlocks;
    }

    public int maxSearchBlocks() {
        return maxSearchBlocks;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility);
    }

    @Override
    protected boolean hasSpecialAbilityContext(BlockState state, BlockPos pos, Player player) {
        return maxSearchBlocks > 0 && state.is(BlockTags.LOGS);
    }

    @Override
    protected void performSpecialAbility(ItemStack stack, Level world, BlockState state, BlockPos pos,
        ServerPlayer player) {
        var frontier = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        var logs = new ArrayList<BlockPos>();
        frontier.add(pos);
        visited.add(pos);
        var matchedBlocks = 1;
        while (!frontier.isEmpty() && matchedBlocks < maxSearchBlocks) {
            var current = frontier.removeFirst();
            for (var direction : Direction.values()) {
                if (matchedBlocks >= maxSearchBlocks) {
                    break;
                }
                var candidate = current.relative(direction);
                if (!visited.add(candidate) || !world.hasChunk(SectionPos.blockToSectionCoord(candidate.getX()),
                    SectionPos.blockToSectionCoord(candidate.getZ()))) {
                    continue;
                }
                var candidateState = world.getBlockState(candidate);
                if (candidateState.is(BlockTags.LOGS)) {
                    matchedBlocks++;
                    frontier.addLast(candidate);
                    logs.add(candidate);
                } else if (candidateState.is(BlockTags.LEAVES)) {
                    matchedBlocks++;
                    frontier.addLast(candidate);
                }
            }
        }
        destroyExtraBlocks(player, logs);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if (getPower(stack) < normalUseCost()) {
            return InteractionResult.PASS;
        }
        var result = applyAction(context, ItemAbilities.AXE_STRIP, SoundEvents.AXE_STRIP, 0);
        if (result.consumesAction()) {
            return result;
        }
        result = applyAction(context, ItemAbilities.AXE_SCRAPE, SoundEvents.AXE_SCRAPE, 3005);
        if (result.consumesAction()) {
            return result;
        }
        return applyAction(context, ItemAbilities.AXE_WAX_OFF, SoundEvents.AXE_WAX_OFF, 3004);
    }

    private InteractionResult applyAction(UseOnContext context, ItemAbility action, SoundEvent sound,
        int levelEvent) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        BlockState modifiedState = state.getToolModifiedState(context, action, false);
        if (modifiedState == null || !level.setBlock(pos, modifiedState, 11)) {
            return InteractionResult.PASS;
        }
        var player = context.getPlayer();
        level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (levelEvent != 0) {
            level.levelEvent(player, levelEvent, pos, 0);
        }
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modifiedState));
        if (!level.isClientSide) {
            charge(context.getItemInHand(), -normalUseCost());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static Tool tool(Config config) {
        return new Tool(List.of(
            Tool.Rule.deniesDrops(config.harvestTier().getIncorrectBlocksForDrops()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, config.miningSpeed())), 1.0F, 0);
    }
}
