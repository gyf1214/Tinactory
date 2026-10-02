package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllDataComponents.DRILL_HIT_FACE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredDrillItem extends PoweredToolItem {
    private final int areaMiningRadius;

    public PoweredDrillItem(Item.Properties properties, PoweredToolConfig config, int areaMiningRadius) {
        super(properties.component(DataComponents.TOOL, tool(config)), config);
        this.areaMiningRadius = areaMiningRadius;
    }

    public int areaMiningRadius() {
        return areaMiningRadius;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility);
    }

    @Override
    protected boolean hasSpecialAbilityContext(BlockState state, BlockPos pos, Player player) {
        return areaMiningRadius > 0 && player.hasData(DRILL_HIT_FACE);
    }

    @Override
    protected void performSpecialAbility(ItemStack stack, Level world, BlockState state, BlockPos pos,
        ServerPlayer player) {
        var face = player.getExistingDataOrNull(DRILL_HIT_FACE.get());
        if (face == null) {
            return;
        }
        var planeA = switch (face.getAxis()) {
            case X, Z -> Direction.UP;
            case Y -> Direction.NORTH;
        };
        var planeB = switch (face.getAxis()) {
            case X -> Direction.SOUTH;
            case Y, Z -> Direction.EAST;
        };
        var candidates = new ArrayList<BlockPos>();
        for (var x = -areaMiningRadius; x <= areaMiningRadius; x++) {
            for (var y = -areaMiningRadius; y <= areaMiningRadius; y++) {
                if (x == 0 && y == 0) {
                    continue;
                }
                var target = pos.relative(planeA, x).relative(planeB, y);
                if (!world.hasChunk(SectionPos.blockToSectionCoord(target.getX()),
                    SectionPos.blockToSectionCoord(target.getZ()))) {
                    continue;
                }
                var targetState = world.getBlockState(target);
                if (targetState.is(BlockTags.MINEABLE_WITH_PICKAXE) ||
                    targetState.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                    candidates.add(target);
                }
            }
        }
        destroyExtraBlocks(player, candidates);
    }

    @Override
    protected void breakAttemptFinished(ServerPlayer player) {
        player.removeData(DRILL_HIT_FACE.get());
    }

    private static Tool tool(PoweredToolConfig config) {
        return new Tool(List.of(
            Tool.Rule.deniesDrops(config.harvestTier().getIncorrectBlocksForDrops()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, config.miningSpeed()),
            Tool.Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, config.miningSpeed())), 1.0F, 0);
    }
}
