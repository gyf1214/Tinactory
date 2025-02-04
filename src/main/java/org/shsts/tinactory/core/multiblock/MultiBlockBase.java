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

import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllEvents.SERVER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlockBase extends UpdatableCapabilityProvider
    implements IEventSubscriber {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int CHECK_CYCLE = 40;

    public final BlockEntity blockEntity;
    protected MultiBlockManager manager;
    @Nullable
    protected WeakMap.Ref<MultiBlockBase> ref = null;
    private int checkTick = 0;
    private boolean preInvalid = false;

    public MultiBlockBase(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public <K> void addToMap(WeakMap<K, MultiBlockBase> map, K key) {
        if (ref == null) {
            ref = map.put(key, this);
        } else {
            map.put(key, ref);
        }
    }

    protected abstract Optional<Collection<BlockPos>> checkMultiBlock();

    protected void onInvalidate() {}

    public void markPreInvalid() {
        LOGGER.debug("{} mark pre invalid", this);
        preInvalid = true;
    }

    public void invalidate() {
        if (ref != null) {
            ref.invalidate();
            ref = null;
            checkTick = 0;
            onInvalidate();
        }
    }

    protected void onRegister() {}

    private void onServerLoad(Level world) {
        manager = MultiBlockManager.get(world);
    }

    private void onRemove() {
        invalidate();
    }

    private void onServerTick() {
        if (preInvalid && checkMultiBlock().isEmpty()) {
            invalidate();
        }
        preInvalid = false;
        if (ref != null) {
            return;
        }
        if (++checkTick > CHECK_CYCLE) {
            checkMultiBlock().ifPresent(blocks -> {
                manager.register(this, blocks);
                onRegister();
            });
            checkTick = 0;
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
