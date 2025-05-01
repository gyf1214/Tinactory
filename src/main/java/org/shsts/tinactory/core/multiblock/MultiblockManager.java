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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final WeakMap<BlockPos, MultiblockBase> posMap = new WeakMap<>();

    public void destroy() {
        posMap.clear();
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

    private static final Map<ResourceKey<Level>, MultiblockManager> INSTANCES = new HashMap<>();

    public static MultiblockManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, $ -> new MultiblockManager());
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
