package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.core.network.Network;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Machine that can run without a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveMachine extends Machine {
    public PrimitiveMachine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Ignore global workFactor.
     */
    @Override
    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workSpeed = TinactoryConfig.INSTANCE.primitiveWorkSpeed.get();
        getProcessor().ifPresent(processor -> processor.onWorkTick(workSpeed));
    }

    @Override
    protected void onServerTick(Level world, BlockPos pos, BlockState state) {
        if (network == null) {
            var workSpeed = TinactoryConfig.INSTANCE.primitiveWorkSpeed.get();
            getProcessor().ifPresent(processor -> {
                processor.onPreWork();
                processor.onWorkTick(workSpeed);
            });
        }
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return network == null || super.canPlayerInteract(player);
    }
}
