package org.shsts.tinactory.content.logistics;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.annotation.Nullable;
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
    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    public record PortKey(UUID machineId, int portIndex) {}

    public record PortInfo(IMachine machine, int portIndex, IPort port, @Nullable BlockPos subnet) {}

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Multimap<BlockPos, PortKey> subnetPorts = HashMultimap.create();
    private final Set<PortKey> globalPorts = new HashSet<>();

    public void registerPort(BlockPos subnet, IMachine machine, int index, IPort port) {
        var key = new PortKey(machine.uuid(), index);
        ports.put(key, new PortInfo(machine, index, port, subnet));
        subnetPorts.put(subnet, key);
    }

    public void registerGlobalPort(IMachine machine, int index, IPort port) {
        var key = new PortKey(machine.uuid(), index);
        ports.put(key, new PortInfo(machine, index, port, null));
        globalPorts.add(key);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            ports.remove(key);
            if (info.subnet != null) {
                subnetPorts.remove(info.subnet, key);
            } else {
                globalPorts.remove(key);
            }
        }
    }

    public Collection<PortInfo> getGlobalPorts() {
        return globalPorts.stream()
            .map(ports::get)
            .toList();
    }

    public Collection<PortInfo> getVisiblePorts(BlockPos subnet) {
        var ret = new ArrayList<PortInfo>();
        globalPorts.forEach(key -> ret.add(ports.get(key)));
        subnetPorts.get(subnet).forEach(key -> ret.add(ports.get(key)));
        return ret;
    }

    public boolean hasPort(PortKey key) {
        return ports.containsKey(key);
    }

    public Optional<PortInfo> getPort(PortKey key) {
        return Optional.ofNullable(ports.get(key));
    }

    @Override
    public void onDisconnect() {
        ports.clear();
        globalPorts.clear();
        subnetPorts.clear();
    }

    private void onTick(Level world, INetwork network) {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }
}
