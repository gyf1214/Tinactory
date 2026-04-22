package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import java.util.Collection;
import java.util.Optional;

import static org.shsts.tinactory.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SERVER_TICK;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiblockBase extends UpdatableCapabilityProvider
    implements IEventSubscriber, IMultiblock {
    public final BlockEntity blockEntity;
    protected MultiblockManager manager;
    protected final MultiblockRuntime runtime;

    public MultiblockBase(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.runtime = new MultiblockRuntime(this, CONFIG.multiblockCheckCycle.get());
    }

    protected abstract Optional<Collection<BlockPos>> checkMultiblock();

    protected void onInvalidate() {}

    public void invalidate() {
        runtime.invalidate();
    }

    protected void onRegister() {}

    @Override
    public Optional<Collection<BlockPos>> checkStructure() {
        return checkMultiblock();
    }

    @Override
    public void onRegisterStructure() {
        onRegister();
    }

    @Override
    public void onInvalidateStructure() {
        onInvalidate();
    }

    protected void registerCleanroom(BlockPos center, int w, int d, int h) {
        manager.registerCleanroom(runtime, center, w, d, h);
    }

    private void onServerLoad(Level world) {
        manager = MultiblockManager.get(world);
    }

    private void onRemove() {
        invalidate();
    }

    protected void onServerTick() {
        runtime.tick(manager);
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
        return getClass().getSimpleName() + "[" + blockEntity + "]";
    }
}
