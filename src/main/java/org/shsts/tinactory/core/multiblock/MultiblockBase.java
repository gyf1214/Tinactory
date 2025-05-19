package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.common.WeakMap;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SERVER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiblockBase extends UpdatableCapabilityProvider
    implements IEventSubscriber {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final BlockEntity blockEntity;
    protected MultiblockManager manager;
    @Nullable
    private WeakMap.Ref<MultiblockBase> ref = null;
    private int checkTick = 0;
    private boolean preInvalid = false;

    public MultiblockBase(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public <K> void addToMap(WeakMap<K, MultiblockBase> map, K key) {
        if (ref == null) {
            ref = map.put(key, this);
        } else {
            map.put(key, ref);
        }
    }

    protected abstract Optional<Collection<BlockPos>> checkMultiblock();

    protected void onInvalidate() {}

    public void markPreInvalid() {
        LOGGER.debug("{} mark pre invalid", this);
        preInvalid = true;
    }

    public void invalidate() {
        if (ref != null) {
            LOGGER.debug("{} invalidate", this);
            ref.invalidate();
            ref = null;
            checkTick = CONFIG.multiblockCheckCycle.get();
            onInvalidate();
        }
    }

    protected void onRegister() {}

    private void onServerLoad(Level world) {
        manager = MultiblockManager.get(world);
    }

    private void onRemove() {
        invalidate();
    }

    protected void onServerTick() {
        if (preInvalid && checkMultiblock().isEmpty()) {
            invalidate();
        }
        preInvalid = false;
        if (ref != null) {
            return;
        }
        if (--checkTick < 0) {
            checkMultiblock().ifPresent(blocks -> {
                manager.register(this, blocks);
                onRegister();
            });
            checkTick = CONFIG.multiblockCheckCycle.get();
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), this::onServerLoad);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), $ -> onRemove());
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), $ -> onRemove());
        eventManager.subscribe(SERVER_TICK.get(), $ -> onServerTick());
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), blockEntity);
    }
}
