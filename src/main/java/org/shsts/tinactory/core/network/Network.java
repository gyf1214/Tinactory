package org.shsts.tinactory.core.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Network extends NetworkBase implements INetwork {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<IComponentType<?>, INetworkComponent> components = new HashMap<>();
    private final Multimap<BlockPos, IMachine> subnetMachines = ArrayListMultimap.create();
    private final Map<BlockPos, BlockPos> blockSubnets = new HashMap<>();
    private final Multimap<IScheduling, INetworkComponent.Ticker> componentSchedulings =
        ArrayListMultimap.create();
    private final Multimap<IScheduling, INetworkComponent.Ticker> machineSchedulings =
        ArrayListMultimap.create();

    public Network(Level world, BlockPos center, TeamProfile team) {
        super(world, center, team);
        attachComponents();
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

    @Override
    protected void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        super.putBlock(pos, state, subnet);
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

    @Override
    protected void connectFinish() {
        super.connectFinish();
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

    @Override
    protected void onDisconnect(boolean connected) {
        // if the network is not ever connected, skip callbacks of the machines and components.
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
        super.onDisconnect(connected);
    }

    @Override
    protected void doTick() {
        super.doTick();
        for (var scheduling : SchedulingManager.getSortedSchedulings()) {
            for (var entry : componentSchedulings.get(scheduling)) {
                entry.tick(world, this);
            }
            for (var ticker : machineSchedulings.get(scheduling)) {
                ticker.tick(world, this);
            }
        }
    }
}
