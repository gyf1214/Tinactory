package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.common.WeakMap;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    private final ResourceKey<Level> dimension;
    private final WeakMap<BlockPos, MultiblockRuntime> posMap = new WeakMap<>();
    private final WeakMap<BlockPos, MultiblockRuntime> cleanroomMap = new WeakMap<>();

    public MultiblockManager() {
        this.dimension = null;
    }

    public MultiblockManager(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void destroy() {
        posMap.clear();
        cleanroomMap.clear();
    }

    public boolean register(MultiblockRuntime runtime, Collection<BlockPos> blocks) {
        LOGGER.debug("register new multi block {}", runtime.host());
        for (var pos : blocks) {
            if (posMap.get(pos).isPresent()) {
                return false;
            }
        }
        for (var pos : blocks) {
            runtime.addToMap(posMap, pos);
        }
        return true;
    }

    public void invalidate(BlockPos pos) {
        posMap.get(pos).ifPresent(MultiblockRuntime::markStructureDirty);
    }

    /*
     * TODO: Optimize ME
     */
    public void registerCleanroom(MultiblockRuntime runtime, BlockPos center, int w, int d, int h) {
        LOGGER.debug("register new cleanroom {}, size={}x{}x{}", runtime.host(), 2 * w - 1, h - 1, 2 * d - 1);
        for (var x = -w + 1; x <= w - 1; x++) {
            for (var z = -d + 1; z <= d - 1; z++) {
                for (var y = 1; y < h; y++) {
                    var pos = center.offset(x, -y, z);
                    if (cleanroomMap.get(pos).isPresent()) {
                        var dimensionName = dimension == null ? "<unknown>" : dimension.location();
                        LOGGER.warn("Cleanroom conflict at {}:{}", dimensionName, pos);
                    }
                    runtime.addToMap(cleanroomMap, pos);
                }
            }
        }
    }

    public Optional<IMultiblock> getCleanroom(BlockPos pos) {
        return cleanroomMap.get(pos).map(MultiblockRuntime::host);
    }
}
