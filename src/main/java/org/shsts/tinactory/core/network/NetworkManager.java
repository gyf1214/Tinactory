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

    protected final Level world;
    protected final Map<BlockPos, NetworkBase> networks = new HashMap<>();
    protected final Map<BlockPos, NetworkBase.Ref> networkPosMap = new HashMap<>();

    public NetworkManager(Level world) {
        LOGGER.debug("create network manager for {}", world.dimension());
        this.world = world;
    }

    public boolean hasNetworkAtPos(BlockPos pos) {
        return this.getNetworkAtPos(pos).isPresent();
    }

    public Optional<NetworkBase> getNetworkAtPos(BlockPos pos) {
        return Optional.ofNullable(this.networks.get(pos)).or(() ->
                Optional.ofNullable(this.networkPosMap.get(pos)).flatMap(NetworkBase.Ref::get));
    }

    public void putNetworkAtPos(BlockPos pos, NetworkBase network) {
        assert !this.hasNetworkAtPos(pos);
        LOGGER.debug("track block at {}:{} to network {}", world.dimension(), pos, network);
        this.networkPosMap.put(pos, network.ref());
    }

    public boolean registerNetwork(NetworkBase network) {
        var center = network.center;
        if (this.networks.containsKey(center)) {
            return this.networks.get(center) == network;
        } else {
            LOGGER.debug("register network {} at center {}:{}", network, world.dimension(), center);
            this.invalidatePos(center);
            this.networks.put(center, network);
            return true;
        }
    }

    public void unregisterNetwork(NetworkBase network) {
        var center = network.center;
        this.networks.remove(center);
    }

    public void invalidatePos(BlockPos pos) {
        this.getNetworkAtPos(pos).ifPresent(NetworkBase::invalidate);
    }

    public void invalidatePosDir(BlockPos pos, Direction dir) {
        var pos1 = pos.relative(dir);
        this.invalidatePos(pos);
        this.invalidatePos(pos1);
    }

    public void destroy() {
        this.networkPosMap.clear();
        this.networks.clear();
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
