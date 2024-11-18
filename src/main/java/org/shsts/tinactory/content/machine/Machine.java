package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.logistics.LogisticsComponent;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider
    implements IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final SmartBlockEntity blockEntity;

    @Nullable
    protected Network network;

    private UUID uuid = UUID.randomUUID();
    public final MachineConfig config = new MachineConfig();

    protected Machine(SmartBlockEntity be) {
        this.blockEntity = be;
    }

    private void onRemoved(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("{}: removed in world", this);
    }

    /**
     * Called only on server
     */
    public void setConfig(SetMachineConfigPacket packet) {
        config.apply(packet);
        if (packet.isSetPort()) {
            updatePassiveRequests();
        }
        sendUpdate(blockEntity);
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    protected void onServerUse(AllEvents.OnUseArg arg, ReturnEvent.Token<InteractionResult> token) {
        var player = arg.player();
        if (!canPlayerInteract(player)) {
            token.setReturn(InteractionResult.FAIL);
            return;
        }

        var item = player.getItemInHand(arg.hand());
        if (item.is(Items.NAME_TAG) && item.hasCustomHoverName()) {
            if (!player.level.isClientSide) {
                var name = Component.Serializer.toJson(item.getHoverName());
                setConfig(SetMachineConfigPacket.builder().set("name", name).create());
                item.shrink(1);
            }

            token.setReturn(InteractionResult.sidedSuccess(player.level.isClientSide));
            return;
        }

        token.setReturn(InteractionResult.PASS);
    }

    private void addActiveRequests(IContainer container, LogisticsComponent logistics) {
        var size = container.portSize();
        for (var i = 0; i < size; i++) {
            if (!container.hasPort(i) || config.getPortConfig("portConfig_" + i) !=
                MachineConfig.PortConfig.ACTIVE ||
                container.portDirection(i) != PortDirection.OUTPUT) {
                continue;
            }
            var port = container.getPort(i, false);
            if (port.type() == PortType.ITEM) {
                logistics.addActiveItem(PortDirection.OUTPUT, port.asItem());
            } else if (port.type() == PortType.FLUID) {
                logistics.addActiveFluid(PortDirection.OUTPUT, port.asFluid());
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

            if (config.getPortConfig("portConfig_" + i) == MachineConfig.PortConfig.PASSIVE) {
                logistics.addPassivePort(direction, port);
            } else {
                logistics.removePassivePort(direction, port);
            }
        }
    }

    public Component getTitle() {
        return config.getString("name")
            .map(Component.Serializer::fromJson)
            .orElseGet(() -> I18n.name(blockEntity.getBlockState().getBlock()));
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.SERVER_USE, this::onServerUse);
    }

    protected Optional<BlockState> getWorkBlock(Level world) {
        return blockEntity.getRealBlockState();
    }

    protected void setWorkBlock(Level world, BlockState state) {
        world.setBlock(blockEntity.getBlockPos(), state, 3);
    }

    protected void updateWorkBlock(Level world, boolean working) {
        getWorkBlock(world)
            .filter(state -> state.hasProperty(WORKING) && state.getValue(WORKING) != working)
            .ifPresent(state -> setWorkBlock(world, state.setValue(WORKING, working)));
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(Network network) {
        LOGGER.debug("{}: connect to network {}", this, network);
        this.network = network;
        updatePassiveRequests();
        EventManager.invoke(blockEntity, AllEvents.CONNECT, network);
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        network = null;
        LOGGER.trace("{}: disconnect from network", this);
        var world = blockEntity.getLevel();
        assert world != null;
        updateWorkBlock(world, false);
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
        var workFactor1 = Math.pow(workFactor, TinactoryConfig.INSTANCE.workFactorExponent.get());
        getProcessor().ifPresent(processor -> {
            processor.onWorkTick(workFactor1);
            updateWorkBlock(world, processor.getProgress() > 0d);
        });
    }

    public void buildSchedulings(NetworkComponent.SchedulingBuilder builder) {
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

    public void sendUpdate() {
        var level = blockEntity.getLevel();
        if (level != null && !level.isClientSide) {
            sendUpdate(blockEntity);
        }
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
        tag.putUUID("uuid", uuid);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        config.deserializeNBT(tag.getCompound("config"));
        uuid = tag.getUUID("uuid");
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

    protected static final String ID = "network/machine";

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID, Machine::new);
    }

    public static Optional<IProcessor> getProcessor(BlockEntity be) {
        if (be instanceof PrimitiveMachine) {
            return AllCapabilities.PROCESSOR.tryGet(be);
        }
        return AllCapabilities.MACHINE.tryGet(be).flatMap(Machine::getProcessor);
    }
}
