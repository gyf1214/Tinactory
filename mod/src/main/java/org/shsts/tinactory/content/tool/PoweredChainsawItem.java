package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredChainsawItem extends PoweredToolItem {
    private final int maxSearchBlocks;

    public PoweredChainsawItem(Item.Properties properties, PoweredToolConfig config, int maxSearchBlocks) {
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

    private static Tool tool(PoweredToolConfig config) {
        return new Tool(List.of(
            Tool.Rule.deniesDrops(config.harvestTier().getIncorrectBlocksForDrops()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, config.miningSpeed())), 1.0F, 0);
    }
}
