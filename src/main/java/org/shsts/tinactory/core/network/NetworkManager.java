package org.shsts.tinactory.core.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Level world;
    private final Map<BlockPos, NetworkBase> networks = new HashMap<>();
    private final Map<BlockPos, NetworkBase.Ref> networkPosMap = new HashMap<>();

    public NetworkManager(Level world) {
        LOGGER.debug("create network manager for {}", world.dimension());
        this.world = world;
    }

    public boolean hasNetworkAtPos(BlockPos pos) {
        return getNetworkAtPos(pos).isPresent();
    }

    public Optional<NetworkBase> getNetworkAtPos(BlockPos pos) {
        return Optional.ofNullable(networks.get(pos)).or(() ->
                Optional.ofNullable(networkPosMap.get(pos)).flatMap(NetworkBase.Ref::get));
    }

    public void putNetworkAtPos(BlockPos pos, NetworkBase network) {
        assert !hasNetworkAtPos(pos);
        LOGGER.debug("track block at {}:{} to network {}", world.dimension(), pos, network);
        networkPosMap.put(pos, network.ref());
    }

    public boolean registerNetwork(NetworkBase network) {
        var center = network.center;
        if (networks.containsKey(center)) {
            return networks.get(center) == network;
        } else {
            LOGGER.debug("register network {} at center {}:{}", network, world.dimension(), center);
            invalidatePos(center);
            networks.put(center, network);
            return true;
        }
    }

    public void unregisterNetwork(NetworkBase network) {
        var center = network.center;
        networks.remove(center);
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

    private static final Map<ResourceKey<Level>, NetworkManager> MANAGERS = new HashMap<>();

    public static NetworkManager getInstance(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return MANAGERS.computeIfAbsent(dimension, $ -> new NetworkManager(world));
    }

    public static Optional<NetworkManager> tryGetInstance(Level world) {
        return world.isClientSide ? Optional.empty() : Optional.of(getInstance(world));
    }

    public static void onUnload(Level world) {
        LOGGER.debug("remove network manager for {}", world.dimension());
        var manager = MANAGERS.get(world.dimension());
        if (manager != null) {
            manager.destroy();
            MANAGERS.remove(world.dimension());
        }
    }
}
