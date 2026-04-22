package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.machine.IMachine;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMultiblockCheckCtx<U> {
    boolean isFailed();

    void setFailed(boolean val);

    default void setFailed() {
        setFailed(true);
    }

    BlockPos getCenter();

    Optional<U> getBlock(BlockPos pos);

    Optional<IMachine> getMachine(BlockPos pos);

    Optional<Direction> getFacing();

    void addBlock(BlockPos pos);

    Object getProperty(String key);

    void setProperty(String key, Object val);

    void deleteProperty(String key);

    boolean hasProperty(String key);
}
