package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.AllDataComponents;
import org.shsts.tinactory.integration.material.MaterialSet;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredToolItem extends PoweredItem {
    private final long normalUseCost;
    private final long specialAbilityCost;
    private final float miningSpeed;
    private final Tier harvestTier;
    private final MaterialSet material;

    public PoweredToolItem(Properties properties, PoweredToolConfig config) {
        super(properties, config.voltage(), config.capacity());
        this.normalUseCost = config.normalUseCost();
        this.specialAbilityCost = config.specialAbilityCost();
        this.miningSpeed = config.miningSpeed();
        this.harvestTier = config.harvestTier();
        this.material = config.material();
    }

    public long normalUseCost() {
        return normalUseCost;
    }

    public long specialAbilityCost() {
        return specialAbilityCost;
    }

    public float miningSpeed() {
        return miningSpeed;
    }

    public Tier harvestTier() {
        return harvestTier;
    }

    public MaterialSet material() {
        return material;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        var stack = player.getMainHandItem();
        if (stack.getItem() != this) {
            return false;
        }
        if (player.hasData(AllDataComponents.POWERED_TOOL_BREAK_GUARD.get())) {
            return true;
        }
        return getPower(stack) >= actionCost(stack, state, pos, player);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos,
        LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                if (player.hasData(AllDataComponents.POWERED_TOOL_BREAK_GUARD.get())) {
                    return true;
                }
                var cost = actionCost(stack, state, pos, player);
                var useSpecialAbility = cost == specialAbilityCost && specialAbilityCost > 0;
                try {
                    charge(stack, -cost);
                    if (useSpecialAbility) {
                        performSpecialAbility(stack, level, state, pos, player);
                    }
                } finally {
                    breakAttemptFinished(player);
                }
            } else {
                charge(stack, -normalUseCost);
            }
        }
        return true;
    }

    protected boolean hasSpecialAbilityContext(BlockState state, BlockPos pos, Player player) {
        return false;
    }

    protected void performSpecialAbility(ItemStack stack, Level level, BlockState state, BlockPos pos,
        ServerPlayer player) {}

    protected void breakAttemptFinished(ServerPlayer player) {}

    protected final void destroyExtraBlocks(ServerPlayer player, List<BlockPos> positions) {
        if (positions.isEmpty()) {
            return;
        }
        var guard = AllDataComponents.POWERED_TOOL_BREAK_GUARD.get();
        player.setData(guard, Unit.INSTANCE);
        try {
            for (var pos : positions) {
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            player.removeData(guard);
        }
    }

    private long actionCost(ItemStack stack, BlockState state, BlockPos pos, Player player) {
        if (specialAbilityCost > 0 && player.isShiftKeyDown() && hasSpecialAbilityContext(state, pos, player) &&
            getPower(stack) >= specialAbilityCost) {
            return specialAbilityCost;
        }
        return normalUseCost;
    }
}
