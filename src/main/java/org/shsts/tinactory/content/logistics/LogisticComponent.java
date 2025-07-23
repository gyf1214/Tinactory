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

    public record PortInfo(IMachine machine, int portIndex, IPort port) {}

    private static class Subnet {
        private final Set<PortKey> subnetPorts = new HashSet<>();
        private final Set<PortKey> storagePorts = new HashSet<>();

        public void registerSubnetPort(PortKey key) {
            subnetPorts.add(key);
        }

        public void registerStoragePort(PortKey key) {
            storagePorts.add(key);
        }

        public void unregisterPort(PortKey key) {
            subnetPorts.remove(key);
            storagePorts.remove(key);
        }
    }

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Map<BlockPos, Subnet> subnets = new HashMap<>();
    private final Set<PortKey> globalPorts = new HashSet<>();
    private final Map<BlockPos, Runnable> onUpdatePorts = new HashMap<>();

    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    private Subnet getSubnet(BlockPos subnet) {
        return subnets.computeIfAbsent(subnet, $ -> new Subnet());
    }

    private BlockPos getMachineSubnet(IMachine machine) {
        return network.getSubnet(machine.blockEntity().getBlockPos());
    }

    private void invokeUpdate(BlockPos subnet) {
        // TODO: allow multiple callbacks
        if (onUpdatePorts.containsKey(subnet)) {
            onUpdatePorts.get(subnet).run();
        }
    }

    private PortKey registerPort(IMachine machine, int index, IPort port) {
        var key = new PortKey(machine.uuid(), index);
        assert !ports.containsKey(key);
        ports.put(key, new PortInfo(machine, index, port));
        return key;
    }

    public void registerPort(IMachine machine, int index, IPort port,
        boolean isGlobal, boolean isStorage) {
        var subnet = getMachineSubnet(machine);
        var key = registerPort(machine, index, port);
        var logisticSubnet = getSubnet(subnet);
        logisticSubnet.registerSubnetPort(key);
        if (isGlobal) {
            globalPorts.add(key);
        }
        if (isStorage) {
            logisticSubnet.registerStoragePort(key);
        }
        invokeUpdate(subnet);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            var subnet = getMachineSubnet(info.machine);
            ports.remove(key);
            globalPorts.remove(key);
            getSubnet(subnet).unregisterPort(key);
            invokeUpdate(subnet);
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

    public void onUpdatePorts(BlockPos subnet, Runnable callback) {
        // TODO: allow multiple callbacks
        onUpdatePorts.put(subnet, callback);
    }

    @Override
    public void onDisconnect() {
        ports.clear();
        globalPorts.clear();
        subnets.clear();
        onUpdatePorts.clear();
    }

    private void onTick(Level world, INetwork network) {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }
}
