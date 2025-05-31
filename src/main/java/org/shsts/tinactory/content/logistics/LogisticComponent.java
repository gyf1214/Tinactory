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

import java.util.ArrayList;
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

    public record PortInfo(IMachine machine, int portIndex, IPort port, BlockPos subnet, boolean isGlobal) {}

    private static class Subnet {
        private final Set<PortKey> subnetPorts = new HashSet<>();
        private final Set<PortKey> storagePorts = new HashSet<>();

        public void registerSubnetPort(PortKey key) {
            subnetPorts.add(key);
        }

        public void registerStoragePort(PortKey key) {
            storagePorts.add(key);
        }

        public Collection<PortKey> subnetPorts() {
            return subnetPorts;
        }

        public Collection<PortKey> storagePorts() {
            return storagePorts;
        }

        public void unregisterPort(PortKey key) {
            subnetPorts.remove(key);
            storagePorts.remove(key);
        }
    }

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Map<BlockPos, Subnet> subnets = new HashMap<>();
    private final Set<PortKey> globalPorts = new HashSet<>();

    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    private Subnet getSubnet(BlockPos subnet) {
        return subnets.computeIfAbsent(subnet, $ -> new Subnet());
    }

    /**
     * Call this function first before calling {@link #registerStoragePort} if you want both.
     */
    public void registerPort(BlockPos subnet, IMachine machine, int index, IPort port, boolean isGlobal) {
        var key = new PortKey(machine.uuid(), index);
        assert !ports.containsKey(key);
        ports.put(key, new PortInfo(machine, index, port, subnet, isGlobal));
        if (isGlobal) {
            globalPorts.add(key);
        } else {
            getSubnet(subnet).registerSubnetPort(key);
        }
    }

    public void registerStoragePort(BlockPos subnet, IMachine machine, int index, IPort port) {
        var key = new PortKey(machine.uuid(), index);
        if (!ports.containsKey(key)) {
            ports.put(key, new PortInfo(machine, index, port, subnet, false));
        }
        getSubnet(subnet).registerStoragePort(key);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            ports.remove(key);
            getSubnet(info.subnet).unregisterPort(key);
            globalPorts.remove(key);
        }
    }

    public Collection<PortInfo> getVisiblePorts(BlockPos subnet) {
        var ret = new ArrayList<PortInfo>();
        globalPorts.forEach(key -> ret.add(ports.get(key)));
        getSubnet(subnet).subnetPorts().forEach(key -> ret.add(ports.get(key)));
        return ret;
    }

    public Collection<IPort> getStoragePorts(BlockPos subnet) {
        return getSubnet(subnet).storagePorts().stream()
            .map(key -> ports.get(key).port())
            .toList();
    }

    public Optional<PortInfo> getPort(PortKey key) {
        return Optional.ofNullable(ports.get(key));
    }

    @Override
    public void onDisconnect() {
        ports.clear();
        globalPorts.clear();
        subnets.clear();
    }

    private void onTick(Level world, INetwork network) {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }
}
