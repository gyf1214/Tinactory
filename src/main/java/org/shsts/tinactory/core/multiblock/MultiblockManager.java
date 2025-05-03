package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.common.WeakMap;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ResourceKey<Level> dimension;
    private final WeakMap<BlockPos, MultiblockBase> posMap = new WeakMap<>();
    private final WeakMap<BlockPos, MultiblockBase> cleanroomMap = new WeakMap<>();

    public MultiblockManager(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void destroy() {
        posMap.clear();
        cleanroomMap.clear();
    }

    public boolean register(MultiblockBase multiblock, Collection<BlockPos> blocks) {
        LOGGER.debug("register new multi block {}", multiblock);
        for (var pos : blocks) {
            if (posMap.get(pos).isPresent()) {
                return false;
            }
        }
        for (var pos : blocks) {
            multiblock.addToMap(posMap, pos);
        }
        return true;
    }

    public void invalidate(BlockPos pos) {
        posMap.get(pos).ifPresent(MultiblockBase::markPreInvalid);
    }

    public void registerCleanroom(MultiblockBase multiblock, BlockPos center, int w, int d, int h) {
        LOGGER.debug("register new cleanroom {}, size={}x{}x{}", multiblock, 2 * w - 1, h - 1, 2 * d - 1);
        for (var x = -w + 1; x <= w - 1; x++) {
            for (var z = -d + 1; z <= d - 1; z++) {
                for (var y = 1; y < h; y++) {
                    var pos = center.offset(x, y, z);
                    if (cleanroomMap.get(pos).isPresent()) {
                        LOGGER.warn("Cleanroom conflict at {}:{}", dimension.location(), pos);
                    }
                    multiblock.addToMap(cleanroomMap, pos);
                }
            }
        }
    }

    public Optional<MultiblockBase> getCleanroom(BlockPos pos) {
        return cleanroomMap.get(pos);
    }

    private static final Map<ResourceKey<Level>, MultiblockManager> INSTANCES = new HashMap<>();

    public static MultiblockManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, MultiblockManager::new);
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
