package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.NetworkComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.shsts.tinactory.content.AllNetworks.LOGISTICS_SCHEDULING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticComponent extends NetworkComponent {
    public record PortKey(UUID machineId, int portIndex) {}

    public record PortInfo(IMachine machine, int portIndex, IPort port, Collection<BlockPos> subnets) {}

    private static class Subnet {
        private final Set<PortKey> subnetPorts = new HashSet<>();
        private final Set<PortKey> storagePorts = new HashSet<>();

        public void registerSubnetPort(PortKey key, boolean isStorage) {
            subnetPorts.add(key);
            if (isStorage) {
                storagePorts.add(key);
            }
        }

        public void unregisterPort(PortKey key) {
            subnetPorts.remove(key);
            storagePorts.remove(key);
        }
    }

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Map<BlockPos, Subnet> subnets = new HashMap<>();
    private final Set<PortKey> globalPorts = new HashSet<>();
    private final Set<Runnable> callbacks = new HashSet<>();

    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    private Subnet getSubnet(BlockPos subnet) {
        return subnets.computeIfAbsent(subnet, $ -> new Subnet());
    }

    private BlockPos getMachineSubnet(IMachine machine) {
        return network.getSubnet(machine.blockEntity().getBlockPos());
    }

    private PortKey createPort(IMachine machine, int index, IPort port, Collection<BlockPos> subnets) {
        var key = new PortKey(machine.uuid(), index);
        assert !ports.containsKey(key);
        ports.put(key, new PortInfo(machine, index, port, subnets));
        return key;
    }

    public void registerPortInSubnets(IMachine machine, int index, IPort port,
        boolean isGlobal, Object... subnetOpts) {
        var subnets = new HashSet<BlockPos>();
        for (var i = 0; i < subnetOpts.length; i += 2) {
            var subnet = (BlockPos) subnetOpts[i];
            subnets.add(subnet);
        }
        var key = createPort(machine, index, port, subnets);
        if (isGlobal) {
            globalPorts.add(key);
        }
        for (var i = 0; i < subnetOpts.length; i += 2) {
            var subnet = (BlockPos) subnetOpts[i];
            var isStorage = (boolean) subnetOpts[i + 1];
            getSubnet(subnet).registerSubnetPort(key, isStorage);
        }
        invokeUpdate();
    }

    public void registerPort(IMachine machine, int index, IPort port,
        boolean isGlobal, boolean isStorage) {
        registerPortInSubnets(machine, index, port, isGlobal,
            getMachineSubnet(machine), isStorage);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            globalPorts.remove(key);
            ports.remove(key);
            for (var subnet : info.subnets) {
                getSubnet(subnet).unregisterPort(key);
            }
            invokeUpdate();
        }
    }

    public Collection<PortInfo> getVisiblePorts(BlockPos subnet) {
        var keys = new HashSet<PortKey>();
        keys.addAll(globalPorts);
        keys.addAll(getSubnet(subnet).subnetPorts);

        return keys.stream().map(ports::get).toList();
    }

    public Collection<IPort> getStoragePorts(BlockPos subnet) {
        return getSubnet(subnet).storagePorts.stream()
            .map(key -> ports.get(key).port())
            .toList();
    }

    public Optional<PortInfo> getPort(PortKey key, BlockPos subnet) {
        if (globalPorts.contains(key) || getSubnet(subnet).subnetPorts.contains(key)) {
            return Optional.ofNullable(ports.get(key));
        } else {
            return Optional.empty();
        }
    }

    private void invokeUpdate() {
        for (var cb : callbacks) {
            cb.run();
        }
    }

    public void onUpdatePorts(Runnable cb) {
        callbacks.add(cb);
    }

    public void unregisterCallback(Runnable cb) {
        callbacks.remove(cb);
    }

    @Override
    public void onDisconnect() {
        ports.clear();
        globalPorts.clear();
        subnets.clear();
        callbacks.clear();
    }

    private void onTick(Level world, INetwork network) {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }
}
