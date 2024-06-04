package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.LogisticsComponent;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider
        implements IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final SmartBlockEntity blockEntity;

    @Nullable
    private Network network;

    public final MachineConfig config = new MachineConfig();

    private Machine(SmartBlockEntity be) {
        this.blockEntity = be;
    }

    private void onRemoved(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("machine {}: removed in world", this);
    }

    /**
     * Called only on server
     */
    public void setConfig(SetMachinePacket packet) {
        config.apply(packet);
        updatePassiveRequests();
        sendUpdate(blockEntity);
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    private void onServerUse(AllEvents.OnUseArg arg, ReturnEvent.Token<InteractionResult> token) {
        if (!canPlayerInteract(arg.player())) {
            token.setReturn(InteractionResult.FAIL);
        } else {
            token.setReturn(InteractionResult.PASS);
        }
    }

    private void addActiveRequests(IContainer container, LogisticsComponent logistics) {
        var size = container.portSize();
        for (var i = 0; i < size; i++) {
            if (!container.hasPort(i) || config.getPortConfig(i) != MachineConfig.PortConfig.ACTIVE ||
                    container.portDirection(i) != PortDirection.OUTPUT) {
                continue;
            }
            var port = container.getPort(i, false);
            if (port.type() == PortType.ITEM) {
                for (var stack : port.asItem().getAllItems()) {
                    logistics.addActiveItem(PortDirection.OUTPUT, port.asItem(), stack);
                }
            } else if (port.type() == PortType.FLUID) {
                for (var stack : port.asFluid().getAllFluids()) {
                    logistics.addActiveFluid(PortDirection.OUTPUT, port.asFluid(), stack);
                }
            }
        }
    }

    private void updatePassiveRequests() {
        if (network == null) {
            return;
        }
        var container1 = getContainer();
        if (container1.isEmpty()) {
            return;
        }
        var container = container1.get();
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);

        var size = container.portSize();
        for (var i = 0; i < size; i++) {
            if (!container.hasPort(i)) {
                continue;
            }

            var direction = container.portDirection(i);
            var port = container.getPort(i, false);

            if (config.getPortConfig(i) == MachineConfig.PortConfig.PASSIVE) {
                logistics.addPassiveStorage(direction, port);
            } else {
                logistics.removePassiveStorage(direction, port);
            }
        }
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.SERVER_USE, this::onServerUse);
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(Network network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
        updatePassiveRequests();
        EventManager.invoke(blockEntity, AllEvents.CONNECT, network);
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }

    private void onPreWork(Level world, Network network) {
        assert this.network == network;
        getProcessor().ifPresent(IProcessor::onPreWork);
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        getContainer().ifPresent(container -> addActiveRequests(container, logistics));
    }

    private void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        getProcessor().ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(Component.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EventManager.invoke(blockEntity, AllEvents.BUILD_SCHEDULING, builder);
    }

    public Optional<Network> getNetwork() {
        return Optional.ofNullable(network);
    }

    public Optional<TeamProfile> getOwnerTeam() {
        if (network == null) {
            return Optional.empty();
        } else {
            return Optional.of(network.team);
        }
    }

    public Optional<IProcessor> getProcessor() {
        return AllCapabilities.PROCESSOR.tryGet(blockEntity);
    }

    public Optional<IContainer> getContainer() {
        return AllCapabilities.CONTAINER.tryGet(blockEntity);
    }

    public Optional<IElectricMachine> getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.tryGet(blockEntity);
    }

    public Optional<LogisticsComponent> getLogistics() {
        return getNetwork().map(network -> network.getComponent(AllNetworks.LOGISTICS_COMPONENT));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("config", config.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        config.deserializeNBT(tag.getCompound("config"));
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        return serializeNBT();
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        deserializeNBT(tag);
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "network/machine", Machine::new);
    }
}
