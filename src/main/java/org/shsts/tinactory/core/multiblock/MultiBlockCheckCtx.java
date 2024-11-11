package org.shsts.tinactory.core.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface MultiBlockCheckCtx {
    boolean isFailed();

    void setFailed();

    BlockPos getCenter();

    Optional<BlockState> getBlock(BlockPos pos);

    Optional<BlockEntity> getBlockEntity(BlockPos pos);

    void addBlock(BlockPos pos);

    Object getProperty(String key);

    void setProperty(String key, Object val);

    boolean hasProperty(String key);
}
