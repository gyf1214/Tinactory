package org.shsts.tinactory.core.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkController extends Machine {
    @Nullable
    private Network network = null;

    public NetworkController(BlockEntityType<NetworkController> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onServerLoad(Level world) {
        super.onServerLoad(world);
        assert network == null;
        network = new Network(world, worldPosition);
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (network != null) {
            network.tick();
        }
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (network != null) {
            network.destroy();
        }
        super.onRemovedInWorld(world);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        if (network != null) {
            network.destroy();
        }
        super.onRemovedByChunk(world);
    }
}
