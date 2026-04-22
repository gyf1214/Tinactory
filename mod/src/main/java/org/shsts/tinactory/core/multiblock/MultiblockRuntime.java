package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.WeakMap;
import org.slf4j.Logger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockRuntime {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final IMultiblock host;
    private final int checkCycle;
    @Nullable
    private WeakMap.Ref<MultiblockRuntime> ref = null;
    private int checkTick = 0;
    private boolean structureDirty = false;

    public MultiblockRuntime(IMultiblock host, int checkCycle) {
        this.host = host;
        this.checkCycle = checkCycle;
    }

    public IMultiblock host() {
        return host;
    }

    <K> void addToMap(WeakMap<K, MultiblockRuntime> map, K key) {
        if (ref == null) {
            ref = map.put(key, this);
        } else {
            map.put(key, ref);
        }
    }

    public void markStructureDirty() {
        LOGGER.trace("{} mark structure dirty", host);
        structureDirty = true;
    }

    public void invalidate() {
        if (ref != null) {
            LOGGER.debug("{} invalidate", host);
            ref.invalidate();
            ref = null;
            checkTick = checkCycle;
            host.onInvalidateStructure();
        }
        structureDirty = false;
    }

    public void tick(MultiblockManager manager) {
        if (structureDirty) {
            if (host.checkStructure().isEmpty()) {
                invalidate();
            }
            structureDirty = false;
        }
        if (ref != null) {
            return;
        }
        if (--checkTick < 0) {
            host.checkStructure().ifPresent(blocks -> {
                if (manager.register(this, blocks)) {
                    host.onRegisterStructure();
                }
            });
            checkTick = checkCycle;
        }
    }
}
