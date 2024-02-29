package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.CompositeNetwork;
import org.shsts.tinactory.core.network.Network;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends SmartBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected CompositeNetwork network;

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void onLoad(Level world) {
        LOGGER.debug("machine {}: loaded", this);
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (this.network != null) {
            this.network.invalidate();
        }
        super.onRemovedInWorld(world);
        LOGGER.debug("machine {}: removed in world", this);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        if (this.network != null) {
            this.network.invalidate();
        }
        LOGGER.debug("machine {}: removed by chunk unload", this);
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(CompositeNetwork network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
    }

    protected void onPreWork(Level world, Network network) {
        this.getCapability(AllCapabilities.PROCESSOR.get()).ifPresent(IProcessor::onPreWork);
    }

    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = this.network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        this.getCapability(AllCapabilities.PROCESSOR.get())
                .ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Component.Ticker> cons) {
        cons.accept(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        cons.accept(AllNetworks.WORK_SCHEDULING, this::onWork);
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        this.network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }
}
