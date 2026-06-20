package org.shsts.tinactory.core.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.network.INetworkTicker;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.network.ISubnetLabel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class NetworkRuntime {
    private record SubnetKey(ISubnetLabel label, BlockPos pos) {}

    private final INetwork host;
    private final List<IScheduling> sortedSchedulings;
    private final Map<IComponentType<?>, INetworkComponent> components = new HashMap<>();
    private final Collection<IMachine> machines = new ArrayList<>();
    private final Set<BlockPos> blocks = new LinkedHashSet<>();
    private final Map<SubnetKey, BlockPos> blockSubnets = new HashMap<>();
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

    public Collection<IMachine> allMachines() {
        return machines;
    }

    public BlockPos getSubnet(BlockPos pos, ISubnetLabel label) {
        return blockSubnets.get(new SubnetKey(label, pos));
    }

    public Collection<BlockPos> allBlocks() {
        return blocks;
    }

    public void putMachine(IMachine machine) {
        machine.assignNetwork(host);
        machines.add(machine);
    }

    public void putBlock(BlockPos pos, Collection<ISubnetLabel> labels,
        Function<ISubnetLabel, BlockPos> subnets, Consumer<INetworkComponent> componentCallback) {
        blocks.add(pos);
        for (var label : labels) {
            blockSubnets.put(new SubnetKey(label, pos), subnets.apply(label));
        }
        for (var component : components.values()) {
            componentCallback.accept(component);
        }
    }

    public void onConnectFinished(Function<INetworkTicker, Runnable> tickerActionFactory) {
        for (var component : components.values()) {
            component.onConnect();
        }
        for (var machine : machines) {
            machine.onConnectToNetwork(host);
        }
        for (var component : components.values()) {
            component.onPostConnect();
        }
        for (var machine : machines) {
            machine.buildSchedulings((scheduling, ticker) -> {
                machineSchedulings.put(scheduling, tickerActionFactory.apply(ticker));
            });
        }
    }

    public void onDisconnect(boolean connected) {
        if (connected) {
            for (var machine : machines) {
                machine.onDisconnectFromNetwork();
            }
            for (var component : components.values()) {
                component.onDisconnect();
            }
        }
        machines.clear();
        blocks.clear();
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
