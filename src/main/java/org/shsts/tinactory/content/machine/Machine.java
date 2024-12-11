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
import net.minecraft.world.item.ItemStack;
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
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_USE;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider
    implements IEventSubscriber, INBTSerializable<CompoundTag> {
    protected static final String ID = "network/machine";
    private static final Logger LOGGER = LogUtils.getLogger();

    public final BlockEntity blockEntity;

    @Nullable
    protected Network network;

    private UUID uuid = UUID.randomUUID();
    public final MachineConfig config = new MachineConfig();

    protected Machine(BlockEntity be) {
        this.blockEntity = be;
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, Machine::new);
    }

    private void onRemoved(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("{}: removed in world", this);
    }

    /**
     * Called only on server.
     */
    public void setConfig(SetMachineConfigPacket packet, boolean invokeEvent) {
        config.apply(packet);
        sendUpdate(blockEntity);
        if (invokeEvent) {
            EVENT_MANAGER.get(blockEntity).invoke(SET_MACHINE_CONFIG.get());
        }
    }

    /**
     * Called only on server.
     */
    public void setConfig(SetMachineConfigPacket packet) {
        setConfig(packet, true);
    }

    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    protected void onServerUse(AllEvents.OnUseArg arg,
        IReturnEvent.Result<InteractionResult> result) {
        var player = arg.player();
        if (!canPlayerInteract(player)) {
            result.set(InteractionResult.FAIL);
            return;
        }

        var item = player.getItemInHand(arg.hand());
        if (item.is(Items.NAME_TAG) && item.hasCustomHoverName()) {
            if (!player.level.isClientSide) {
                var name = Component.Serializer.toJson(item.getHoverName());
                setConfig(SetMachineConfigPacket.builder().set("name", name).get());
                item.shrink(1);
            }

            result.set(InteractionResult.sidedSuccess(player.level.isClientSide));
            return;
        }

        result.set(InteractionResult.PASS);
    }

    public Component getTitle() {
        return config.getString("name")
            .map(Component.Serializer::fromJson)
            .orElseGet(() -> I18n.name(blockEntity.getBlockState().getBlock()));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(SERVER_USE.get(), this::onServerUse);
    }

    protected static Optional<BlockState> getRealBlockState(Level world, BlockEntity be) {
        var pos = be.getBlockPos();
        if (!world.isLoaded(pos)) {
            return Optional.empty();
        }
        var state = be.getBlockState();
        if (!world.getBlockState(pos).is(state.getBlock())) {
            return Optional.empty();
        }
        return Optional.of(state);
    }

    protected Optional<BlockState> getWorkBlock(Level world) {
        return getRealBlockState(world, blockEntity);
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

        getContainer().ifPresent(container -> {
            var subnet = network.getSubnet(blockEntity.getBlockPos());
            var logistics = network.getComponent(AllNetworks.LOGISTIC_COMPONENT);
            for (var i = 0; i < container.portSize(); i++) {
                if (!container.hasPort(i)) {
                    continue;
                }
                logistics.registerPort(subnet, this, i, container.getPort(i, false));
            }
        });

        EVENT_MANAGER.get(blockEntity).invoke(CONNECT.get(), network);
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
    }

    private void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        var machineSpeed = MathUtil.safePow(workFactor, TinactoryConfig.INSTANCE.workFactorExponent.get());
        getProcessor().ifPresent(processor -> {
            processor.onWorkTick(machineSpeed);
            updateWorkBlock(world, processor.getProgress() > 0d);
        });
    }

    public void buildSchedulings(NetworkComponent.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EVENT_MANAGER.get(blockEntity).invoke(BUILD_SCHEDULING.get(), builder);
    }

    public UUID getUuid() {
        return uuid;
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
        return PROCESSOR.tryGet(blockEntity);
    }

    public Optional<IContainer> getContainer() {
        return AllCapabilities.CONTAINER.tryGet(blockEntity);
    }

    public Optional<IElectricMachine> getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.tryGet(blockEntity);
    }

    public ItemStack getIcon() {
        var block = blockEntity.getBlockState().getBlock();
        return new ItemStack(block);
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
        if (cap == MACHINE.get()) {
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
        EVENT_MANAGER.get(blockEntity).invoke(SET_MACHINE_CONFIG.get());
    }

    public static Optional<IProcessor> getProcessor(BlockEntity be) {
        var machine = MACHINE.tryGet(be);
        return machine.map(Machine::getProcessor)
            .orElseGet(() -> PROCESSOR.tryGet(be));
    }
}
