package org.shsts.tinactory.content.logistics;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticComponent extends NetworkComponent {
    public LogisticComponent(ComponentType<LogisticComponent> type, Network network) {
        super(type, network);
    }

    public record PortKey(UUID machineId, int portIndex) {}

    public record PortInfo(Machine machine, int portIndex, IPort port, @Nullable BlockPos subnet) {}

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Multimap<BlockPos, PortKey> subnetPorts = HashMultimap.create();
    private final Set<PortKey> globalPorts = new HashSet<>();

    public void registerPort(BlockPos subnet, Machine machine, int index, IPort port) {
        var key = new PortKey(machine.getUuid(), index);
        ports.put(key, new PortInfo(machine, index, port, subnet));
        subnetPorts.put(subnet, key);
    }

    public void registerGlobalPort(Machine machine, int index, IPort port) {
        var key = new PortKey(machine.getUuid(), index);
        ports.put(key, new PortInfo(machine, index, port, null));
        globalPorts.add(key);
    }

    public void unregisterPort(Machine machine, int index) {
        var key = new PortKey(machine.getUuid(), index);
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

    private void onTick(Level world, Network network) {}

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }
}
