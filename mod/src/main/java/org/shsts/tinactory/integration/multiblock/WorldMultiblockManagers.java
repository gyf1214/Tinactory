package org.shsts.tinactory.integration.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.multiblock.MultiblockManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class WorldMultiblockManagers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceKey<Level>, MultiblockManager> INSTANCES = new HashMap<>();

    private WorldMultiblockManagers() {}

    public static MultiblockManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, $ -> {
            LOGGER.debug("create multi block manager for {}", dimension.location());
            return new MultiblockManager(dimension);
        });
    }

    public static Optional<MultiblockManager> tryGet(Level world) {
        return world.isClientSide ? Optional.empty() : Optional.of(get(world));
    }

    public static void onUnload(Level world) {
        LOGGER.debug("remove multi block manager for {}", world.dimension().location());
        var manager = INSTANCES.get(world.dimension());
        if (manager != null) {
            manager.destroy();
            INSTANCES.remove(world.dimension());
        }
    }
}
