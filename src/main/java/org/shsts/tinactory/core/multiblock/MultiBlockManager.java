package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.common.WeakMap;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiBlockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final WeakMap<BlockPos, MultiBlockBase> posMap = new WeakMap<>();

    public void destroy() {
        posMap.clear();
    }

    public boolean register(MultiBlockBase multiBlock, Collection<BlockPos> blocks) {
        LOGGER.debug("register new multi block {}", multiBlock);
        for (var pos : blocks) {
            if (posMap.get(pos).isPresent()) {
                return false;
            }
        }
        for (var pos : blocks) {
            multiBlock.addToMap(posMap, pos);
        }
        return true;
    }

    public void invalidate(BlockPos pos) {
        posMap.get(pos).ifPresent(MultiBlockBase::markPreInvalid);
    }

    private static final Map<ResourceKey<Level>, MultiBlockManager> INSTANCES = new HashMap<>();

    public static MultiBlockManager get(Level world) {
        assert !world.isClientSide;
        var dimension = world.dimension();
        return INSTANCES.computeIfAbsent(dimension, $ -> new MultiBlockManager());
    }

    public static void onUnload(Level world) {
        LOGGER.debug("remove multi block manager for {}", world.dimension());
        var manager = INSTANCES.get(world.dimension());
        if (manager != null) {
            manager.destroy();
            INSTANCES.remove(world.dimension());
        }
    }
}
