package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.Lazy;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllEvents.SERVER_PLACE;
import static org.shsts.tinactory.content.AllEvents.SERVER_USE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmartEntityBlock extends Block implements EntityBlock {
    private final Supplier<IBlockEntityType> entityType;
    @Nullable
    private final IMenuType menu;

    @FunctionalInterface
    public interface Factory<U extends SmartEntityBlock> {
        U create(Properties properties, Supplier<IBlockEntityType> entityType, @Nullable IMenuType menu);
    }

    protected SmartEntityBlock(Properties properties,
        Supplier<IBlockEntityType> entityType, @Nullable IMenuType menu) {
        super(properties);
        this.entityType = Lazy.of(entityType);
        this.menu = menu;
        registerDefaultState(createDefaultBlockState());
    }

    protected BlockState createDefaultBlockState() {
        return stateDefinition.any();
    }

    public BlockEntityType<?> getType() {
        return entityType.get().get();
    }

    public Optional<BlockEntity> getBlockEntity(Level world, BlockPos pos) {
        if (world.isLoaded(pos)) {
            return world.getBlockEntity(pos, getType())
                .map($ -> $);
        }
        return Optional.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entityType.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T1 extends BlockEntity> BlockEntityTicker<T1> getTicker(
        Level world, BlockState state, BlockEntityType<T1> type) {
        return type == getType() ? entityType.get().ticker() : null;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
        @Nullable LivingEntity placer, ItemStack stack) {
        var be = getBlockEntity(world, pos);
        if (be.isPresent() && !world.isClientSide) {
            CapabilityProvider.invoke(be.get(), SERVER_PLACE,
                new AllEvents.OnPlaceArg(placer, stack));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
        InteractionHand hand, BlockHitResult hitResult) {
        var be = getBlockEntity(world, pos);
        if (be.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide) {
            var args = new AllEvents.OnUseArg(player, hand, hitResult);
            var result = CapabilityProvider.invokeReturn(be.get(), SERVER_USE, args);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }

        if (menu == null) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            menu.open(serverPlayer, pos);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}
