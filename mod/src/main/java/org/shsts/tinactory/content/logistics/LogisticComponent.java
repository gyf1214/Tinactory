package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.integration.network.ComponentType;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticComponent extends NotifierComponent {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record PortKey(UUID machineId, int portIndex) {}

    public record PortInfo(IMachine machine, int portIndex, IPort<?> port, BlockPos subnet, int priority) {}

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Set<PortKey> storagePorts = new HashSet<>();

    public LogisticComponent(ComponentType<LogisticComponent> type, INetwork network) {
        super(type, network);
    }

    private PortKey createPort(IMachine machine, int index, IPort<?> port, BlockPos subnet, int priority) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            LOGGER.warn("duplicate port key {}", key);
        }
        ports.put(key, new PortInfo(machine, index, port, subnet, priority));
        return key;
    }

    private void registerPortInSubnet(IMachine machine, int index, IPort<?> port,
        BlockPos subnet, int priority) {
        var key = createPort(machine, index, port, subnet, priority);
        if (priority >= 0) {
            storagePorts.add(key);
        }
        invokeUpdate();
    }

    public void registerPort(IMachine machine, int index, IPort<?> port) {
        registerPortInSubnet(machine, index, port, getMachineSubnet(machine),
            -1);
    }

    public void registerStoragePort(IMachine machine, int index, IPort<?> port,
        int priority) {
        registerPortInSubnet(machine, index, port, getMachineSubnet(machine),
            priority);
    }

    public void unregisterPort(IMachine machine, int index) {
        var key = new PortKey(machine.uuid(), index);
        if (ports.containsKey(key)) {
            storagePorts.remove(key);
            ports.remove(key);
            invokeUpdate();
        }
    }

    public Collection<PortInfo> getVisiblePorts() {
        return ports.values().stream().toList();
    }

    public Collection<PortInfo> getAllPorts() {
        return ports.values().stream().toList();
    }

    public Collection<? extends IPort<?>> getStoragePorts() {
        return storagePorts.stream()
            .map(ports::get)
            .sorted(Comparator.comparing(PortInfo::priority).reversed())
            .map(PortInfo::port)
            .toList();
    }

    public Optional<PortInfo> getPort(PortKey key) {
        return Optional.ofNullable(ports.get(key));
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        ports.clear();
        storagePorts.clear();
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {}
}
