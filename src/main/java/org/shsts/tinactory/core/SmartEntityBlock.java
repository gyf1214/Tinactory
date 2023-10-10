package org.shsts.tinactory.core;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SmartEntityBlock<T extends SmartBlockEntity> extends Block implements EntityBlock {
    protected final Supplier<SmartBlockEntityType<T>> entityType;

    protected SmartEntityBlock(Properties properties, Supplier<SmartBlockEntityType<T>> entityType) {
        super(properties);
        this.entityType = Lazy.of(entityType);
    }

    public Class<T> getEntityClass() {
        return getEntityType().entityClass;
    }

    public SmartBlockEntityType<T> getEntityType() {
        return entityType.get();
    }

    public Optional<T> getBlockEntity(Level world, BlockPos pos) {
        if (world.isLoaded(pos)) {
            var clazz = this.getEntityClass();
            return world.getBlockEntity(pos, this.getEntityType())
                    .flatMap(be -> clazz.isInstance(be) ? Optional.of(clazz.cast(be)) : Optional.empty());
        }
        return Optional.empty();
    }

    public <T1 extends BlockEntity> Optional<T1> getBlockEntity(Level world, BlockPos pos, Class<T1> clazz) {
        return this.getEntityClass().isAssignableFrom(clazz) ?
                this.getBlockEntity(world, pos).map(clazz::cast) : Optional.empty();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return this.getEntityType().create(pos, state);
    }

    @Nullable
    @Override
    public <T1 extends BlockEntity> BlockEntityTicker<T1>
    getTicker(Level world, BlockState state, BlockEntityType<T1> type) {
        return type == this.getEntityType() && this.getEntityType().ticking ? SmartBlockEntity::ticker : null;
    }
}
