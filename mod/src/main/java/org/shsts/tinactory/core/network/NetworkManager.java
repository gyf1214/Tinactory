package org.shsts.tinactory.core.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.core.common.WeakMap;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NetworkManager {
    private final WeakMap<BlockPos, NetworkGraphEngine<?>> networkPosMap = new WeakMap<>();
    private final Map<NetworkGraphEngine<?>, WeakMap.Ref<NetworkGraphEngine<?>>> networkRefs =
        new IdentityHashMap<>();

    public boolean hasNetworkAtPos(BlockPos pos) {
        return getNetworkAtPos(pos).isPresent();
    }

    public Optional<NetworkGraphEngine<?>> getNetworkAtPos(BlockPos pos) {
        return networkPosMap.get(pos);
    }

    public void putNetworkAtPos(BlockPos pos, NetworkGraphEngine<?> network) {
        assert !hasNetworkAtPos(pos);
        var ref = networkRefs.get(network);
        if (ref == null || ref.get().isEmpty()) {
            ref = networkPosMap.put(pos, network);
            networkRefs.put(network, ref);
            return;
        }
        networkPosMap.put(pos, ref);
    }

    public void invalidatePos(BlockPos pos) {
        getNetworkAtPos(pos).ifPresent(network -> {
            network.invalidate();
            var ref = networkRefs.remove(network);
            if (ref != null) {
                ref.invalidate();
            }
        });
    }

    public void invalidatePosDir(BlockPos pos, Direction dir) {
        var pos1 = pos.relative(dir);
        invalidatePos(pos);
        invalidatePos(pos1);
    }

    public void destroy() {
        networkPosMap.clear();
        networkRefs.clear();
    }
}
