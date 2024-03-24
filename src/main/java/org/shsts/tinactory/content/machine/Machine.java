package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllBlockEntityEvents;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.CompositeNetwork;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends SmartBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected CompositeNetwork network;

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static Machine primitive(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new PrimitiveMachine(type, pos, state);
    }

    public static BlockEntityBuilder.Factory<Machine> factory(Voltage voltage) {
        return voltage == Voltage.PRIMITIVE ? Machine::primitive : Machine::new;
    }

    @Override
    protected void onLoad(Level world) {
        super.onLoad(world);
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
        EventManager.invoke(this, AllBlockEntityEvents.CONNECT, network);
    }

    protected void onPreWork(Level world, Network network) {
        this.getProcessor().ifPresent(IProcessor::onPreWork);
    }

    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = this.network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        this.getProcessor().ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(Component.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EventManager.invoke(this, AllBlockEntityEvents.BUILD_SCHEDULING, builder);
    }

    public Optional<IProcessor> getProcessor() {
        return this.getCapability(AllCapabilities.PROCESSOR.get()).resolve();
    }

    public Optional<IElectricMachine> getElectric() {
        return this.getCapability(AllCapabilities.ELECTRIC_MACHINE.get()).resolve();
    }

    public Optional<IContainer> getContainer() {
        return this.getCapability(AllCapabilities.CONTAINER.get()).resolve();
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        this.network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }
}
