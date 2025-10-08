package org.shsts.tinactory.core.machine;

import com.mojang.logging.LogUtils;
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
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllEvents.SERVER_PLACE;
import static org.shsts.tinactory.content.AllEvents.SERVER_USE;
import static org.shsts.tinactory.content.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.content.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.content.AllNetworks.PRE_WORK_SCHEDULING;
import static org.shsts.tinactory.content.AllNetworks.WORK_SCHEDULING;
import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider implements IMachine,
    IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final String ID = "network/machine";

    protected final BlockEntity blockEntity;

    @Nullable
    protected Network network;

    private UUID uuid = UUID.randomUUID();
    protected final MachineConfig config = new MachineConfig();

    protected Machine(BlockEntity be) {
        this.blockEntity = be;
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, Machine::new);
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Optional<ITeamProfile> owner() {
        var world = blockEntity.getLevel();
        assert world != null;
        if (world.isClientSide) {
            return TechManager.localTeam();
        }
        if (network == null) {
            return Optional.empty();
        }
        return Optional.of(network.team);
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    @Override
    public IMachineConfig config() {
        return config;
    }

    @Override
    public void setConfig(ISetMachineConfigPacket packet, boolean invokeEvent) {
        config.apply(packet);
        sendUpdate(blockEntity);
        if (invokeEvent) {
            invoke(blockEntity, SET_MACHINE_CONFIG);
        }
    }

    private void setName(Component name) {
        var jo = Component.Serializer.toJson(name);
        setConfig(SetMachineConfigPacket.builder().set("name", jo).get());
    }

    private void onServerPlace(AllEvents.OnPlaceArg arg) {
        var item = arg.stack();
        if (item.hasCustomHoverName()) {
            setName(item.getHoverName());
        }
    }

    private void onServerUse(AllEvents.OnUseArg arg, IReturnEvent.Result<InteractionResult> result) {
        var player = arg.player();
        if (!canPlayerInteract(player)) {
            result.set(InteractionResult.FAIL);
            return;
        }

        var item = player.getItemInHand(arg.hand());
        if (item.is(Items.NAME_TAG) && item.hasCustomHoverName()) {
            if (!player.level.isClientSide) {
                setName(item.getHoverName());
                item.shrink(1);
            }

            result.set(InteractionResult.sidedSuccess(player.level.isClientSide));
            return;
        }

        result.set(InteractionResult.PASS);
    }

    private void onRemoved(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("{}: removed in world", blockEntity);
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

    @Override
    public Optional<BlockState> workBlock() {
        var world = blockEntity.getLevel();
        assert world != null;
        return workBlock(world);
    }

    protected Optional<BlockState> workBlock(Level world) {
        return getRealBlockState(world, blockEntity);
    }

    protected void setWorkBlock(Level world, BlockState state) {
        // prevent updateShape on neighbor
        world.setBlock(blockEntity.getBlockPos(), state, 19);
    }

    protected void updateWorkBlock(Level world, boolean working) {
        workBlock(world)
            .filter(state -> state.hasProperty(WORKING) && state.getValue(WORKING) != working)
            .ifPresent(state -> setWorkBlock(world, state.setValue(WORKING, working)));
    }

    @Override
    public Component title() {
        return config.getString("name")
            .map(Component.Serializer::fromJson)
            .orElseGet(() -> I18n.name(blockEntity.getBlockState().getBlock()));
    }

    @Override
    public ItemStack icon() {
        var block = blockEntity.getBlockState().getBlock();
        return new ItemStack(block);
    }

    @Override
    public BlockEntity blockEntity() {
        return blockEntity;
    }

    @Override
    public Optional<IProcessor> processor() {
        return PROCESSOR.tryGet(blockEntity);
    }

    @Override
    public Optional<IContainer> container() {
        return CONTAINER.tryGet(blockEntity);
    }

    @Override
    public Optional<IElectricMachine> electric() {
        return ELECTRIC_MACHINE.tryGet(blockEntity);
    }

    @Override
    public Optional<INetwork> network() {
        return Optional.ofNullable(network);
    }

    @Override
    public void onConnectToNetwork(INetwork network) {
        LOGGER.trace("{}: connect to network {}", blockEntity, network);
        this.network = (Network) network;

        container().ifPresent(container -> {
            var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
            for (var i = 0; i < container.portSize(); i++) {
                if (!container.hasPort(i)) {
                    continue;
                }
                logistics.registerPort(this, i, container.getPort(i, false), false, false);
            }
        });

        invoke(blockEntity, CONNECT, network);
    }

    @Override
    public void onDisconnectFromNetwork() {
        network = null;
        LOGGER.trace("{}: disconnect from network", blockEntity);
        var world = blockEntity.getLevel();
        assert world != null;
        updateWorkBlock(world, false);
    }

    private void onPreWork(Level world, INetwork network) {
        assert this.network == network;
        processor().ifPresent(IProcessor::onPreWork);
    }

    private void onWork(Level world, INetwork network) {
        assert this.network == network;
        var workFactor = network
            .getComponent(ELECTRIC_COMPONENT.get())
            .getWorkFactor();
        var machineSpeed = MathUtil.safePow(workFactor, CONFIG.workFactorExponent.get());
        processor().ifPresent(processor -> {
            processor.onWorkTick(machineSpeed);
            updateWorkBlock(world, processor.getProgress() > 0d);
        });
    }

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(PRE_WORK_SCHEDULING.get(), this::onPreWork);
        builder.add(WORK_SCHEDULING.get(), this::onWork);
        invoke(blockEntity, BUILD_SCHEDULING, builder);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(SERVER_PLACE.get(), this::onServerPlace);
        eventManager.subscribe(SERVER_USE.get(), this::onServerUse);
    }

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
        LOGGER.trace("{} deserializer machine NBT, tag={}", blockEntity, tag);
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
        invoke(blockEntity, SET_MACHINE_CONFIG);
    }

    public static Optional<IProcessor> getProcessor(BlockEntity be) {
        var machine = MACHINE.tryGet(be);
        return machine.map(IMachine::processor)
            .orElseGet(() -> PROCESSOR.tryGet(be));
    }
}
