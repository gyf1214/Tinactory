package org.shsts.tinactory.integration.network;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.network.NetworkManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class WorldNetworkManagers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceKey<Level>, NetworkManager> INSTANCES = new HashMap<>();
    private static List<IScheduling> sortedSchedulings = List.of();

    private WorldNetworkManagers() {}

    public static NetworkManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, $ -> {
            LOGGER.debug("create network manager for {}", dimension.location());
            return new NetworkManager();
        });
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

    public static void setSortedSchedulings(List<IScheduling> sortedSchedulings) {
        WorldNetworkManagers.sortedSchedulings = List.copyOf(sortedSchedulings);
    }

    public static List<IScheduling> getSortedSchedulings() {
        return sortedSchedulings;
    }
}
