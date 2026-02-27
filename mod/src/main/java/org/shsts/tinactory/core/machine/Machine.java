package org.shsts.tinactory.core.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import org.shsts.tinactory.AllEvents;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.network.Network;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.shsts.tinactory.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllEvents.BLOCK_PLACE;
import static org.shsts.tinactory.AllEvents.BLOCK_USE;
import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SERVER_TICK;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.PRE_WORK_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.AllNetworks.WORK_SCHEDULING;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.core.network.MachineBlock.WORKING;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider implements IMachine,
    IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String PROGRESS_SIGNAL = "progress";
    public static final String STOP_SIGNAL = "stop";
    protected static final String ID = "network/machine";

    protected final BlockEntity blockEntity;
    protected final MachineConfig config = new MachineConfig();

    private UUID uuid = UUID.randomUUID();
    @Nullable
    protected Network network = null;
    @Nullable
    private String teamName = null;
    @Nullable
    private TeamProfile team = null;

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
        if (world().isClientSide) {
            return TechManager.localTeam();
        }
        return Optional.ofNullable(team);
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return team != null && team.hasPlayer(player);
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

    /**
     * Only called on server
     */
    private void createNetwork(Level world) {
        if (team != null) {
            network = new Network(world, uuid, blockEntity.getBlockPos(), team);
        }
    }

    /**
     * Only called on server
     */
    private void setPlayerTeam(Level world, Player player) {
        TechManager.server().teamByPlayer(player).ifPresent($ -> {
            team = $;
            blockEntity.setChanged();
            createNetwork(world);
        });
    }

    private void onPlace(AllEvents.OnPlaceArg arg) {
        // naming is server only as client gets notified when config is changed.
        if (arg.world().isClientSide) {
            return;
        }
        var item = arg.stack();
        if (item.hasCustomHoverName()) {
            setName(item.getHoverName());
        }
        if (arg.placer() instanceof Player player) {
            setPlayerTeam(arg.world(), player);
        }
    }

    protected void onUse(AllEvents.OnUseArg arg, IReturnEvent.Result<InteractionResult> result) {
        var player = arg.player();
        // TODO: unfortunately client does not know whether the player can interact with this machine,
        //       so on client we simply pass.
        if (player.level.isClientSide) {
            return;
        }

        if (team == null) {
            setPlayerTeam(player.level, player);
        }

        if (!canPlayerInteract(player)) {
            result.set(InteractionResult.FAIL);
            return;
        }

        var item = player.getItemInHand(arg.hand());
        if (item.is(Items.NAME_TAG) && item.hasCustomHoverName()) {
            setName(item.getHoverName());
            item.shrink(1);
            result.set(InteractionResult.sidedSuccess(player.level.isClientSide));
            return;
        }

        result.set(InteractionResult.PASS);
    }

    private void onServerLoad(Level world) {
        team = teamName == null ? null : TechManager.server().teamByName(teamName).orElse(null);
        teamName = null;
        createNetwork(world);
    }

    private void onServerTick(Level world) {
        if (network != null && network.center.equals(blockEntity.getBlockPos())) {
            network.tick();
        }
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
        return workBlock(world());
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
    public void assignNetwork(INetwork net) {
        LOGGER.trace("{}: assign to network {}", blockEntity, net);
        if (network == net) {
            return;
        }
        network = (Network) net;
        team = network.team;
        blockEntity.setChanged();
    }

    @Override
    public void onConnectToNetwork(INetwork network) {
        LOGGER.trace("{}: connect to network {}", blockEntity, network);
        container().ifPresent(container -> {
            var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
            for (var i = 0; i < container.portSize(); i++) {
                if (!container.hasPort(i)) {
                    continue;
                }
                logistics.registerPort(this, i, container.getPort(i, ContainerAccess.EXTERNAL), false);
            }
        });

        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        processor().ifPresent(processor -> signal.registerRead(this, PROGRESS_SIGNAL, () ->
            MathUtil.toSignal(processor.getProgress())));

        invoke(blockEntity, CONNECT, network);
    }

    @Override
    public void onDisconnectFromNetwork() {
        LOGGER.trace("{}: disconnect from network", blockEntity);
        var world = world();
        if (network != null && !network.center.equals(blockEntity.getBlockPos())) {
            network = null;
            createNetwork(world);
        }
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
        var workSpeed = MathUtil.safePow(workFactor, CONFIG.workFactorExponent.get());
        var workSpeed1 = MathUtil.compare(workSpeed) > 0 ? workSpeed : 0d;
        processor().ifPresent(processor -> {
            processor.onWorkTick(workSpeed1);
            updateWorkBlock(world, processor.isWorking(workSpeed1));
        });
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {
        builder.add(PRE_WORK_SCHEDULING.get(), this::onPreWork);
        builder.add(WORK_SCHEDULING.get(), this::onWork);
        invoke(blockEntity, BUILD_SCHEDULING, builder);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(SERVER_LOAD.get(), this::onServerLoad);
        eventManager.subscribe(SERVER_TICK.get(), this::onServerTick);
        eventManager.subscribe(BLOCK_PLACE.get(), this::onPlace);
        eventManager.subscribe(BLOCK_USE.get(), this::onUse);
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
        if (team != null) {
            tag.putString("owner", team.getName());
        } else if (teamName != null) {
            tag.putString("owner", teamName);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        LOGGER.trace("{} deserializer machine NBT, tag={}", blockEntity, tag);
        config.deserializeNBT(tag.getCompound("config"));
        uuid = tag.getUUID("uuid");
        teamName = tag.contains("owner", Tag.TAG_STRING) ? tag.getString("owner") : null;
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

    public static void registerStopSignal(INetwork network, IMachine machine,
        Consumer<Boolean> setter) {
        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerWrite(machine, STOP_SIGNAL, val -> setter.accept(val > 0));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + blockEntity + ", uuid=" + uuid + "]";
    }
}
