package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllBlockEntityEvents;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.Component;
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
    protected Network network;

    public final MachineConfig machineConfig = new MachineConfig();

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static Machine primitive(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new PrimitiveMachine(type, pos, state);
    }

    public static BlockEntityBuilder.Factory<Machine> factory(Voltage voltage) {
        return voltage == Voltage.PRIMITIVE ? Machine::primitive : Machine::new;
    }

    public void setMachineConfig(SetMachinePacket packet) {
        assert level != null && !level.isClientSide;
        machineConfig.apply(level, packet);
        EventManager.invoke(this, AllBlockEntityEvents.SET_MACHINE_CONFIG, packet);
    }

    @Override
    protected void onServerLoad(Level world) {
        machineConfig.onLoad(world);
        super.onServerLoad(world);
        LOGGER.debug("machine {}: loaded", this);
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (network != null) {
            network.invalidate();
        }
        super.onRemovedInWorld(world);
        LOGGER.debug("machine {}: removed in world", this);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("machine {}: removed by chunk unload", this);
    }

    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    @Override
    protected InteractionResult onServerUse(Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!canPlayerInteract(player)) {
            return InteractionResult.FAIL;
        }
        return super.onServerUse(player, hand, hitResult);
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(Network network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
        EventManager.invoke(this, AllBlockEntityEvents.CONNECT, network);
    }

    protected void onPreWork(Level world, Network network) {
        assert this.network == network;
        getProcessor().ifPresent(IProcessor::onPreWork);
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        if (machineConfig.isAutoDumpItem()) {
            EventManager.invoke(this, AllBlockEntityEvents.DUMP_ITEM_OUTPUT, logistics);
        }
        if (machineConfig.isAutoDumpFluid()) {
            EventManager.invoke(this, AllBlockEntityEvents.DUMP_FLUID_OUTPUT, logistics);
        }
    }

    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        getProcessor().ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(Component.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EventManager.invoke(this, AllBlockEntityEvents.BUILD_SCHEDULING, builder);
    }

    public Optional<Network> getNetwork() {
        return Optional.ofNullable(network);
    }

    public Optional<IProcessor> getProcessor() {
        return getCapability(AllCapabilities.PROCESSOR.get()).resolve();
    }

    public Optional<IElectricMachine> getElectric() {
        return getCapability(AllCapabilities.ELECTRIC_MACHINE.get()).resolve();
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }

    @Override
    protected void serializeOnSave(CompoundTag tag) {
        tag.put("config", machineConfig.serializeNBT());
    }

    @Override
    protected void deserializeOnSave(CompoundTag tag) {
        machineConfig.deserializeNBT(tag.getCompound("config"));
    }
}
