package org.shsts.tinactory.core.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.network.INetworkTicker;
import org.shsts.tinactory.api.network.IScheduling;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class NetworkRuntime {
    private final INetwork host;
    private final List<IScheduling> sortedSchedulings;
    private final Map<IComponentType<?>, INetworkComponent> components = new HashMap<>();
    private final Multimap<BlockPos, IMachine> subnetMachines = ArrayListMultimap.create();
    private final Map<BlockPos, BlockPos> blockSubnets = new HashMap<>();
    private final Multimap<IScheduling, Runnable> componentSchedulings = ArrayListMultimap.create();
    private final Multimap<IScheduling, Runnable> machineSchedulings = ArrayListMultimap.create();

    public NetworkRuntime(INetwork host, List<IScheduling> sortedSchedulings) {
        this.host = host;
        this.sortedSchedulings = List.copyOf(sortedSchedulings);
    }

    public void attachComponent(IComponentType<?> type,
        Function<INetworkTicker, Runnable> tickerActionFactory) {
        assert !components.containsKey(type);
        var component = type.create(host);
        components.put(type, component);
        component.buildSchedulings((scheduling, ticker) -> {
            componentSchedulings.put(scheduling, tickerActionFactory.apply(ticker));
        });
    }

    public <T extends INetworkComponent> T getComponent(IComponentType<T> type) {
        return type.clazz().cast(components.get(type));
    }

    public Multimap<BlockPos, IMachine> allMachines() {
        return subnetMachines;
    }

    public BlockPos getSubnet(BlockPos pos) {
        return blockSubnets.get(pos);
    }

    public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
        return blockSubnets.entrySet();
    }

    public void putMachine(BlockPos subnet, IMachine machine) {
        machine.assignNetwork(host);
        subnetMachines.put(subnet, machine);
    }

    public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        blockSubnets.put(pos, subnet);
        for (var component : components.values()) {
            component.putBlock(pos, state, subnet);
        }
    }

    public void onConnectFinished(Function<INetworkTicker, Runnable> tickerActionFactory) {
        for (var component : components.values()) {
            component.onConnect();
        }
        for (var machine : subnetMachines.values()) {
            machine.onConnectToNetwork(host);
        }
        for (var component : components.values()) {
            component.onPostConnect();
        }
        for (var machine : subnetMachines.values()) {
            machine.buildSchedulings((scheduling, ticker) -> {
                machineSchedulings.put(scheduling, tickerActionFactory.apply(ticker));
            });
        }
    }

    public void onDisconnect(boolean connected) {
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
    }

    public void tick() {
        for (var scheduling : sortedSchedulings) {
            for (var action : componentSchedulings.get(scheduling)) {
                action.run();
            }
            for (var action : machineSchedulings.get(scheduling)) {
                action.run();
            }
        }
    }
}
