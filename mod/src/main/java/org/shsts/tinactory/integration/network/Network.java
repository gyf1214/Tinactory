package org.shsts.tinactory.integration.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.INetworkGraphAdapter;
import org.shsts.tinactory.core.network.NetworkGraphEngine;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.core.network.SchedulingManager;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Network implements INetwork {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Level world;
    private final NetworkManager manager;
    private final UUID uuid;
    private final Map<IComponentType<?>, INetworkComponent> components = new HashMap<>();
    private final Multimap<BlockPos, IMachine> subnetMachines = ArrayListMultimap.create();
    private final Map<BlockPos, BlockPos> blockSubnets = new HashMap<>();
    private final Multimap<IScheduling, INetworkComponent.Ticker> componentSchedulings =
        ArrayListMultimap.create();
    private final Multimap<IScheduling, INetworkComponent.Ticker> machineSchedulings =
        ArrayListMultimap.create();
    private final NetworkGraphEngine<BlockState> graphEngine;

    public final BlockPos center;
    public final TeamProfile team;

    private int delayTicks;

    public Network(Level world, UUID uuid, BlockPos center, TeamProfile team) {
        this.world = world;
        this.manager = WorldNetworkManagers.get(world);
        this.uuid = uuid;
        this.center = center;
        this.team = team;
        this.graphEngine = new NetworkGraphEngine<>(uuid, center, new GraphAdapter());
        this.delayTicks = 0;
        attachComponents();
    }

    private final class GraphAdapter implements INetworkGraphAdapter<BlockState> {
        @Override
        public boolean isNodeLoaded(BlockPos pos) {
            return world.isLoaded(pos);
        }

        @Override
        public BlockState getNodeData(BlockPos pos) {
            return world.getBlockState(pos);
        }

        @Override
        public boolean isConnected(BlockPos pos, BlockState data, Direction dir) {
            return IConnector.isConnectedInWorld(world, pos, data, dir);
        }

        @Override
        public boolean isSubnet(BlockPos pos, BlockState data) {
            return IConnector.isSubnetInWorld(world, pos, data);
        }

        @Override
        public void onDiscover(BlockPos pos, BlockState data, BlockPos subnet) {
            Network.this.onDiscover(pos, data, subnet);
        }

        @Override
        public void onConnectFinished() {
            Network.this.onConnectFinished();
        }

        @Override
        public void onDisconnect(boolean connected) {
            Network.this.onDisconnect(connected);
        }
    }

    @Override
    public ITeamProfile owner() {
        return team;
    }

    @Override
    public <T extends INetworkComponent> T getComponent(IComponentType<T> type) {
        return type.clazz().cast(components.get(type));
    }

    protected void attachComponent(IComponentType<?> type) {
        assert !components.containsKey(type);
        var component = type.create(this);
        components.put(type, component);
        component.buildSchedulings(componentSchedulings::put);
    }

    protected void attachComponents() {
        ComponentType.getComponentTypes().forEach(this::attachComponent);
    }

    @Override
    public Multimap<BlockPos, IMachine> allMachines() {
        return subnetMachines;
    }

    @Override
    public BlockPos getSubnet(BlockPos pos) {
        return blockSubnets.get(pos);
    }

    @Override
    public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
        return blockSubnets.entrySet();
    }

    protected void putMachine(BlockPos subnet, IMachine machine) {
        LOGGER.trace("{}: put machine {}", this, machine);
        machine.assignNetwork(this);
        subnetMachines.put(subnet, machine);
    }

    private void onDiscover(BlockPos pos, BlockState state, BlockPos subnet) {
        if (manager.hasNetworkAtPos(pos)) {
            var network1 = manager.getNetworkAtPos(pos).orElseThrow();
            if (comparePriorityAgainst(network1)) {
                LOGGER.debug("{}: invalidate conflict network at {}", this, pos);
                network1.invalidate();
            } else {
                LOGGER.debug("{}: invalidate myself because of conflict at {}", this, pos);
                invalidate();
                return;
            }
        }
        manager.putNetworkAtPos(pos, graphEngine);
        putBlock(pos, state, subnet);
    }

    @SuppressWarnings("unchecked")
    private boolean comparePriorityAgainst(NetworkGraphEngine<?> another) {
        return graphEngine.comparePriority((NetworkGraphEngine<BlockState>) another);
    }

    protected void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        LOGGER.trace("{}: add block {} at {}:{}, subnet = {}", this, state,
            world.dimension(), pos, subnet);
        blockSubnets.put(pos, subnet);
        for (var component : components.values()) {
            component.putBlock(pos, state, subnet);
        }
        if (state.getBlock() instanceof SmartEntityBlock entityBlock) {
            entityBlock.getBlockEntity(world, pos)
                .flatMap(MACHINE::tryGet)
                .ifPresent(machine -> putMachine(subnet, machine));
        }
    }

    private void onConnectFinished() {
        LOGGER.debug("{}: connect finished", this);
        delayTicks = 0;
        LOGGER.debug("{}: {} machines connected", this, subnetMachines.values().size());
        for (var component : components.values()) {
            component.onConnect();
        }
        for (var machine : subnetMachines.values()) {
            machine.onConnectToNetwork(this);
        }
        for (var component : components.values()) {
            component.onPostConnect();
        }
        for (var machine : subnetMachines.values()) {
            machine.buildSchedulings(machineSchedulings::put);
        }
    }

    private void onDisconnect(boolean connected) {
        if (connected) {
            for (var machine : subnetMachines.values()) {
                machine.onDisconnectFromNetwork();
            }
            for (var component : components.values()) {
                component.onDisconnect();
            }
        }
        subnetMachines.clear();
        blockSubnets.clear();
        machineSchedulings.clear();
        LOGGER.debug("{}: disconnect", this);
    }

    public void invalidate() {
        graphEngine.invalidate();
        delayTicks = 0;
        LOGGER.debug("{}: invalidated", this);
    }

    protected void doTick() {
        for (var scheduling : SchedulingManager.getSortedSchedulings()) {
            for (var entry : componentSchedulings.get(scheduling)) {
                entry.tick(world, this);
            }
            for (var ticker : machineSchedulings.get(scheduling)) {
                ticker.tick(world, this);
            }
        }
    }

    private void doConnect() {
        var connectDelay = CONFIG.networkConnectDelay.get();
        var maxConnects = CONFIG.networkMaxConnectsPerTick.get();
        if (delayTicks < connectDelay) {
            delayTicks++;
            return;
        }
        for (var i = 0; i < maxConnects; i++) {
            if (!graphEngine.connectNext()) {
                return;
            }
        }
    }

    public void tick() {
        switch (graphEngine.state()) {
            case CONNECTING -> doConnect();
            case CONNECTED -> doTick();
        }
    }

    @Override
    public String toString() {
        return "Network[uuid=" + uuid + "]";
    }
}
