package org.shsts.tinactory.content.logistics;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
import java.util.Comparator;
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

    public record PortInfo(IMachine machine, int portIndex, IPort port, BlockPos subnet, int priority) {}

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final SetMultimap<BlockPos, PortKey> subnetPorts = HashMultimap.create();
    private final Set<PortKey> storagePorts = new HashSet<>();
    private final Set<PortKey> globalPorts = new HashSet<>();
    private final Set<Runnable> callbacks = new HashSet<>();
    private boolean isConnecting = false;

    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    private BlockPos getMachineSubnet(IMachine machine) {
        return network.getSubnet(machine.blockEntity().getBlockPos());
    }

    private PortKey createPort(IMachine machine, int index, IPort port, BlockPos subnet, int priority) {
        var key = new PortKey(machine.uuid(), index);
        assert !ports.containsKey(key);
        ports.put(key, new PortInfo(machine, index, port, subnet, priority));
        return key;
    }

    private void registerPortInSubnet(IMachine machine, int index, IPort port,
        BlockPos subnet, boolean isGlobal, int priority) {
        var key = createPort(machine, index, port, subnet, priority);
        if (isGlobal) {
            globalPorts.add(key);
        }
        if (priority >= 0) {
            storagePorts.add(key);
        }
        subnetPorts.put(subnet, key);
        invokeUpdate();
    }

    public void registerPort(IMachine machine, int index, IPort port,
        boolean isGlobal) {
        registerPortInSubnet(machine, index, port, getMachineSubnet(machine),
            isGlobal, -1);
    }

    public void registerStoragePort(IMachine machine, int index, IPort port,
        boolean isGlobal, int priority) {
        registerPortInSubnet(machine, index, port, getMachineSubnet(machine),
            isGlobal, priority);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            globalPorts.remove(key);
            storagePorts.remove(key);
            subnetPorts.remove(info.subnet, key);
            ports.remove(key);
            invokeUpdate();
        }
    }

    public Collection<PortInfo> getVisiblePorts(BlockPos subnet) {
        var keys = new HashSet<PortKey>();
        keys.addAll(globalPorts);
        keys.addAll(subnetPorts.get(subnet));
        return keys.stream().map(ports::get).toList();
    }

    public Collection<IPort> getStoragePorts() {
        return storagePorts.stream()
            .map(ports::get)
            .sorted(Comparator.comparing(PortInfo::priority).reversed())
            .map(PortInfo::port)
            .toList();
    }

    public Optional<PortInfo> getPort(PortKey key, BlockPos subnet) {
        var info = ports.get(key);
        if (info != null && (globalPorts.contains(key) || info.subnet.equals(subnet))) {
            return Optional.of(info);
        } else {
            return Optional.empty();
        }
    }

    private void invokeUpdate() {
        if (isConnecting) {
            return;
        }
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
    public void onConnect() {
        isConnecting = true;
    }

    @Override
    public void onPostConnect() {
        isConnecting = false;
        invokeUpdate();
    }

    @Override
    public void onDisconnect() {
        ports.clear();
        subnetPorts.clear();
        globalPorts.clear();
        storagePorts.clear();
        callbacks.clear();
    }

    private void onTick(Level world, INetwork network) {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {
        builder.add(LOGISTICS_SCHEDULING.get(), this::onTick);
    }
}
