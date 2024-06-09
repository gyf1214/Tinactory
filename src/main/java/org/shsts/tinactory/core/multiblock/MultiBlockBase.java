package org.shsts.tinactory.core.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.WeakMap;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlockBase extends CapabilityProvider implements IEventSubscriber {
    private static final int CHECK_CYCLE = 40;

    public final BlockEntity blockEntity;
    protected MultiBlockManager manager;
    @Nullable
    protected WeakMap.Ref<MultiBlockBase> ref = null;
    private int checkTick = 0;

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

    protected abstract boolean checkMultiBlock(Level world, int dx, int dy, int dz,
                                               BlockPos pos, BlockState blockState);

    protected Optional<Collection<BlockPos>> checkMultiBlock(BlockPos start, int tx, int ty, int tz) {
        var world = blockEntity.getLevel();
        if (world == null) {
            return Optional.empty();
        }
        var myPos = blockEntity.getBlockPos();
        var list = new ArrayList<BlockPos>();
        for (var dy = 0; dy < ty; dy++) {
            for (var dx = 0; dx < tx; dx++) {
                for (var dz = 0; dz < tz; dz++) {
                    var pos = start.offset(dx, dy, dz);
                    if (!world.isLoaded(pos)) {
                        return Optional.empty();
                    } else if (pos.equals(myPos)) {
                        continue;
                    }
                    if (!checkMultiBlock(world, dx, dy, dz, pos, world.getBlockState(pos))) {
                        return Optional.empty();
                    }
                    list.add(pos);
                }
            }
        }
        return Optional.of(list);
    }

    protected abstract Optional<Collection<BlockPos>> checkMultiBlock();

    protected void onInvalidate() {}

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
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, $ -> onRemove());
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, $ -> onRemove());
        eventManager.subscribe(AllEvents.SERVER_TICK, $ -> onServerTick());
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), blockEntity);
    }
}
