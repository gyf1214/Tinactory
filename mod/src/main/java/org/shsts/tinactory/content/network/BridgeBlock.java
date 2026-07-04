package org.shsts.tinactory.content.network;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.Lazy;
import org.shsts.tinactory.AllEvents;
import org.shsts.tinactory.api.network.ISubnetLabel;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.common.SmartEntityBlock;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.AllEvents.BLOCK_PLACE;
import static org.shsts.tinactory.AllEvents.BLOCK_USE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_SUBNET;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SUBNET;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BridgeBlock extends SubnetBlock implements EntityBlock {
    private final Supplier<IBlockEntityType> entityType;
    private final Consumer<List<Component>> tooltipBuilder;

    public BridgeBlock(Properties properties, Voltage voltage,
        Supplier<IBlockEntityType> entityType, Consumer<List<Component>> tooltipBuilder) {
        super(properties, voltage, voltage);
        this.entityType = Lazy.of(entityType);
        this.tooltipBuilder = tooltipBuilder;
    }

    /**
     * Menu is ignored
     */
    public static SmartEntityBlock.Factory<BridgeBlock> factory(Voltage voltage,
        Consumer<List<Component>> tooltipBuilder) {
        return (properties, entityType, menu) ->
            new BridgeBlock(properties, voltage, entityType, tooltipBuilder);
    }

    private Optional<BlockEntity> getBlockEntity(Level world, BlockPos pos) {
        if (world.isLoaded(pos)) {
            return world.getBlockEntity(pos, entityType.get().get()).map($ -> $);
        }
        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        addTooltip(tooltip, "machineVoltage", NUMBER_FORMAT.format(voltage.value), voltage.displayName());
        tooltipBuilder.accept(tooltip);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entityType.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level world, BlockState state, BlockEntityType<T> type) {
        return type == entityType.get().get() ? entityType.get().ticker() : null;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
        @Nullable LivingEntity placer, ItemStack stack) {
        getBlockEntity(world, pos).ifPresent(be -> CapabilityProvider
            .invoke(be, BLOCK_PLACE, new AllEvents.OnPlaceArg(world, placer, stack)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hitResult) {
        var be = getBlockEntity(world, pos);
        if (be.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        var args = new AllEvents.OnUseArg(player, stack);
        return CapabilityProvider.invokeReturn(be.get(), BLOCK_USE, args);
    }

    @Override
    public Collection<ISubnetLabel> subnetLabels(Level world, BlockPos pos, BlockState state) {
        return List.of(ELECTRIC_SUBNET.get(), LOGISTICS_SUBNET.get());
    }
}
