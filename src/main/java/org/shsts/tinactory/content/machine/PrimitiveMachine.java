package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.network.Network;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Machine that can run without a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveMachine extends Machine {
    protected PrimitiveMachine(BlockEntity be) {
        super(be);
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
    public void subscribeEvents(EventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(AllEvents.SERVER_TICK, this::onServerTick);
    }

    protected void onServerTick(Level world) {
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
