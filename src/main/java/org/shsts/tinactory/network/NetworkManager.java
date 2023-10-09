package org.shsts.tinactory.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
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
    protected final Map<BlockPos, Network.Ref> networkMap = new HashMap<>();

    public NetworkManager(Level world) {
        LOGGER.debug("create network manager for {}", world.dimension());
        this.world = world;
    }

    public boolean hasNetwork(BlockPos pos) {
        return networkMap.containsKey(pos) && networkMap.get(pos).valid();
    }

    public Optional<Network> getNetwork(BlockPos pos) {
        return Optional.ofNullable(this.networkMap.get(pos)).flatMap(Network.Ref::get);
    }

    public void putNetwork(BlockPos pos, Network network) {
        assert !this.hasNetwork(pos);
        LOGGER.debug("track block at {}:{} to network {}", world.dimension(), pos, network);
        this.networkMap.put(pos, network.ref());
    }

    public void invalidatePos(BlockPos pos) {
        this.getNetwork(pos).ifPresent(Network::invalidate);
    }

    private static final Map<ResourceKey<Level>, NetworkManager> MANAGERS = new HashMap<>();

    public static NetworkManager getInstance(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return MANAGERS.computeIfAbsent(dimension, $ -> new NetworkManager(world));
    }

    public static void onUnload(Level world) {
        LOGGER.debug("remove network manager for {}", world.dimension());
        MANAGERS.remove(world.dimension());
    }
}
