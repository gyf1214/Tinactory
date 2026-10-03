package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.material.MaterialSet;

import java.util.List;

import static org.shsts.tinactory.AllDataComponents.POWERED_ACTIVATED;
import static org.shsts.tinactory.AllDataComponents.POWERED_TOOL_BREAK_GUARD;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PoweredToolItem extends PoweredItem {
    private final long normalUseCost;
    private final long specialAbilityCost;
    private final float miningSpeed;
    private final Tier harvestTier;
    private final MaterialSet material;

    public record Config(Voltage voltage, long capacity, long normalUseCost,
        long specialAbilityCost, float miningSpeed, Tier harvestTier, MaterialSet material) {}

    public PoweredToolItem(Properties properties, Config config) {
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
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        var stack = player.getMainHandItem();
        if (stack.getItem() != this) {
            return false;
        }
        if (player.hasData(POWERED_TOOL_BREAK_GUARD)) {
            return true;
        }
        return getPower(stack) >= actionCost(stack, state, pos, player);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos,
        LivingEntity entity) {
        if (!world.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                if (player.hasData(POWERED_TOOL_BREAK_GUARD)) {
                    return true;
                }
                var useSpecialAbility = shouldUseSpecialAbility(stack, state, pos, player);
                var cost = useSpecialAbility ? specialAbilityCost : normalUseCost;
                try {
                    charge(stack, -cost);
                    if (useSpecialAbility) {
                        performSpecialAbility(stack, world, state, pos, player);
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

    protected abstract boolean hasSpecialAbilityContext(BlockState state, BlockPos pos, Player player);

    protected abstract void performSpecialAbility(ItemStack stack, Level world, BlockState state,
        BlockPos pos, ServerPlayer player);

    protected void breakAttemptFinished(ServerPlayer player) {}

    protected final void destroyExtraBlocks(ServerPlayer player, List<BlockPos> positions) {
        if (positions.isEmpty()) {
            return;
        }
        var guard = POWERED_TOOL_BREAK_GUARD.get();
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
        return shouldUseSpecialAbility(stack, state, pos, player) ? specialAbilityCost : normalUseCost;
    }

    protected boolean specialAbilityActivated(ItemStack stack) {
        return specialAbilityCost > 0 && stack.has(POWERED_ACTIVATED);
    }

    private boolean shouldUseSpecialAbility(ItemStack stack, BlockState state, BlockPos pos, Player player) {
        return specialAbilityActivated(stack) && hasSpecialAbilityContext(state, pos, player) &&
            getPower(stack) >= specialAbilityCost;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand) {
        var stack = player.getItemInHand(usedHand);
        if (specialAbilityCost <= 0) {
            return InteractionResultHolder.pass(stack);
        }
        if (!world.isClientSide) {
            if (stack.has(POWERED_ACTIVATED)) {
                stack.remove(POWERED_ACTIVATED);
            } else {
                stack.set(POWERED_ACTIVATED, Unit.INSTANCE);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    protected void appendSpecialAbilityText(ItemStack stack, List<Component> tooltip) {
        var usages = Math.floorDiv(getPower(stack), specialAbilityCost);
        addTooltip(tooltip, "powered_activated", NUMBER_FORMAT.format(usages));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        if (specialAbilityActivated(stack)) {
            appendSpecialAbilityText(stack, tooltip);
        } else if (normalUseCost > 0) {
            var usages = Math.floorDiv(getPower(stack), normalUseCost);
            addTooltip(tooltip, "powered", NUMBER_FORMAT.format(usages));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
