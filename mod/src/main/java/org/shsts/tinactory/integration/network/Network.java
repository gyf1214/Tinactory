package org.shsts.tinactory.integration.network;

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
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.INetworkGraphAdapter;
import org.shsts.tinactory.core.network.NetworkGraphEngine;
import org.shsts.tinactory.core.network.NetworkRuntime;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Network implements INetwork {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Level world;
    private final UUID uuid;
    private final NetworkRuntime runtime;
    private final NetworkGraphEngine<BlockState> graphEngine;

    public final BlockPos center;
    public final TeamProfile team;

    private int delayTicks;

    public Network(Level world, UUID uuid, BlockPos center, TeamProfile team) {
        this.world = world;
        this.uuid = uuid;
        this.center = center;
        this.team = team;
        this.runtime = new NetworkRuntime(this, WorldNetworkManagers.getSortedSchedulings());
        this.graphEngine = new NetworkGraphEngine<>(uuid, center, WorldNetworkManagers.get(world),
            new GraphAdapter());
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
            Network.this.putBlock(pos, data, subnet);
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
        return runtime.getComponent(type);
    }

    private void attachComponent(IComponentType<?> type) {
        runtime.attachComponent(type, ticker -> () -> ticker.tick(world, this));
    }

    private void attachComponents() {
        ComponentType.getComponentTypes().forEach(this::attachComponent);
    }

    @Override
    public Multimap<BlockPos, IMachine> allMachines() {
        return runtime.allMachines();
    }

    @Override
    public BlockPos getSubnet(BlockPos pos) {
        return runtime.getSubnet(pos);
    }

    @Override
    public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
        return runtime.allBlocks();
    }

    private void putMachine(BlockPos subnet, IMachine machine) {
        LOGGER.trace("{}: put machine {}", this, machine);
        runtime.putMachine(subnet, machine);
    }

    private void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        LOGGER.trace("{}: add block {} at {}:{}, subnet = {}", this, state,
            world.dimension(), pos, subnet);
        runtime.putBlock(pos, state, subnet);
        if (state.getBlock() instanceof SmartEntityBlock entityBlock) {
            entityBlock.getBlockEntity(world, pos)
                .flatMap(MACHINE::tryGet)
                .ifPresent(machine -> putMachine(subnet, machine));
        }
    }

    private void onConnectFinished() {
        LOGGER.debug("{}: connect finished", this);
        delayTicks = 0;
        LOGGER.debug("{}: {} machines connected", this, runtime.allMachines().values().size());
        runtime.onConnectFinished(ticker -> () -> ticker.tick(world, this));
    }

    private void onDisconnect(boolean connected) {
        runtime.onDisconnect(connected);
        LOGGER.debug("{}: disconnect", this);
    }

    public void invalidate() {
        graphEngine.invalidate();
        delayTicks = 0;
        LOGGER.debug("{}: invalidated", this);
    }

    private void doTick() {
        runtime.tick();
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
