package org.shsts.tinactory.content.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.network.Component;
import org.shsts.tinactory.network.CompositeNetwork;
import org.shsts.tinactory.network.Scheduling;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends Machine {
    @Nullable
    private CompositeNetwork network;

    public NetworkController(BlockEntityType<NetworkController> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.network = null;
    }

    @Override
    protected void onLoad(Level world) {
        super.onLoad(world);
        if (!world.isClientSide) {
            assert this.network == null;
            this.network = new CompositeNetwork(world, this.worldPosition);
        }
    }

    public void invalidateNetwork() {
        if (this.network != null) {
            this.network.invalidate();
        }
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (this.network != null) {
            this.network.tick();
        }
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (this.network != null) {
            this.network.destroy();
        }
        super.onRemovedInWorld(world);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        super.onRemovedByChunk(world);
        if (this.network != null) {
            this.network.destroy();
        }
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<Scheduling>, Component.Ticker> cons) {}
}
