package org.shsts.tinactory.core.network;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.common.WeakMap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NetworkManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Level world;
    private final Map<BlockPos, NetworkBase> networks = new HashMap<>();
    private final WeakMap<BlockPos, NetworkBase> networkPosMap = new WeakMap<>();

    public NetworkManager(Level world) {
        LOGGER.debug("create network manager for {}", world.dimension().location());
        this.world = world;
    }

    public boolean hasNetworkAtPos(BlockPos pos) {
        return getNetworkAtPos(pos).isPresent();
    }

    public Optional<NetworkBase> getNetworkAtPos(BlockPos pos) {
        return Optional.ofNullable(networks.get(pos)).or(() -> networkPosMap.get(pos));
    }

    public void putNetworkAtPos(BlockPos pos, NetworkBase network) {
        assert !hasNetworkAtPos(pos);
        LOGGER.trace("track block at {}:{} to network {}", world.dimension().location(), pos, network);
        network.addToMap(networkPosMap, pos);
    }

    public boolean registerNetwork(NetworkBase network) {
        var center = network.center;
        if (networks.containsKey(center)) {
            return networks.get(center) == network;
        } else {
            LOGGER.debug("register network {} at center {}:{}", network, world.dimension().location(), center);
            invalidatePos(center);
            networks.put(center, network);
            return true;
        }
    }

    public void unregisterNetwork(NetworkBase network) {
        networks.remove(network.center);
    }

    public void invalidatePos(BlockPos pos) {
        getNetworkAtPos(pos).ifPresent(NetworkBase::invalidate);
    }

    public void invalidatePosDir(BlockPos pos, Direction dir) {
        var pos1 = pos.relative(dir);
        invalidatePos(pos);
        invalidatePos(pos1);
    }

    public void destroy() {
        networkPosMap.clear();
        networks.clear();
    }

    private static final Map<ResourceKey<Level>, NetworkManager> INSTANCES = new HashMap<>();

    public static NetworkManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, $ -> new NetworkManager(world));
    }

    public static Optional<NetworkManager> tryGet(Level world) {
        return world.isClientSide ? Optional.empty() : Optional.of(get(world));
    }

    public static void onUnload(Level world) {
        LOGGER.debug("remove network manager for {}", world.dimension().location());
        var manager = INSTANCES.get(world.dimension());
        if (manager != null) {
            manager.destroy();
            INSTANCES.remove(world.dimension());
        }
    }
}
