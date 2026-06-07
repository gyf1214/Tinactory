package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.core.common.WeakMap;

import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NetworkManager {
    private final WeakMap<BlockPos, NetworkGraphEngine<?>> networkPosMap = new WeakMap<>();

    public boolean hasNetworkAtPos(BlockPos pos) {
        return getNetworkAtPos(pos).isPresent();
    }

    public Optional<NetworkGraphEngine<?>> getNetworkAtPos(BlockPos pos) {
        return networkPosMap.get(pos);
    }

    public WeakMap.Ref<NetworkGraphEngine<?>> putNetworkAtPos(BlockPos pos, NetworkGraphEngine<?> network) {
        assert !hasNetworkAtPos(pos);
        return networkPosMap.put(pos, network);
    }

    public void putNetworkAtPos(BlockPos pos, WeakMap.Ref<NetworkGraphEngine<?>> ref) {
        assert !hasNetworkAtPos(pos);
        networkPosMap.put(pos, ref);
    }

    public void invalidatePos(BlockPos pos) {
        getNetworkAtPos(pos).ifPresent(NetworkGraphEngine::invalidate);
    }

    public void invalidatePosDir(BlockPos pos, Direction dir) {
        var pos1 = pos.relative(dir);
        invalidatePos(pos);
        invalidatePos(pos1);
    }

    public void destroy() {
        networkPosMap.clear();
    }
}
