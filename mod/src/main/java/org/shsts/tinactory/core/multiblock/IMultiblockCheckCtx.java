package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMultiblockCheckCtx {
    boolean isFailed();

    void setFailed(boolean val);

    default void setFailed() {
        setFailed(true);
    }

    BlockPos getCenter();

    Optional<BlockState> getBlock(BlockPos pos);

    Optional<BlockEntity> getBlockEntity(BlockPos pos);

    void addBlock(BlockPos pos);

    Object getProperty(String key);

    void setProperty(String key, Object val);

    void deleteProperty(String key);

    boolean hasProperty(String key);
}
