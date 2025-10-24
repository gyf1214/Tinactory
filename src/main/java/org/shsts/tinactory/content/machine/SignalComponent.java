package org.shsts.tinactory.content.machine;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.NotifierComponent;
import org.shsts.tinactory.core.network.ComponentType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SignalComponent extends NotifierComponent {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, Map<String, IntSupplier>> readSignals = new HashMap<>();
    private final Map<UUID, Map<String, IntConsumer>> writeSignals = new HashMap<>();
    private final SetMultimap<BlockPos, UUID> subnetMachines = HashMultimap.create();
    private final Map<UUID, IMachine> machines = new HashMap<>();

    public record SignalInfo(IMachine machine, String key, boolean isWrite) {}

    public SignalComponent(ComponentType<?> type, INetwork network) {
        super(type, network);
    }

    public void registerRead(IMachine machine, String key, IntSupplier reader) {
        var uuid = machine.uuid();
        readSignals.computeIfAbsent(uuid, $ -> new HashMap<>()).put(key, reader);
        subnetMachines.put(getMachineSubnet(machine), uuid);
        machines.put(uuid, machine);
        invokeUpdate();
    }

    public void registerWrite(IMachine machine, String key, IntConsumer writer) {
        var uuid = machine.uuid();
        writeSignals.computeIfAbsent(uuid, $ -> new HashMap<>()).put(key, writer);
        subnetMachines.put(getMachineSubnet(machine), uuid);
        machines.put(uuid, machine);
        invokeUpdate();
    }

    public void unregisterMachine(IMachine machine) {
        var uuid = machine.uuid();
        readSignals.remove(uuid);
        writeSignals.remove(uuid);
        subnetMachines.remove(getMachineSubnet(machine), uuid);
        invokeUpdate();
    }

    public Collection<SignalInfo> getSubnetSignals(BlockPos subnet) {
        var ret = new ArrayList<SignalInfo>();
        for (var uuid : subnetMachines.get(subnet)) {
            var machine = machines.get(uuid);
            if (readSignals.containsKey(uuid)) {
                for (var key : readSignals.get(uuid).keySet()) {
                    ret.add(new SignalInfo(machine, key, false));
                }
            }
            if (writeSignals.containsKey(uuid)) {
                for (var key : writeSignals.get(uuid).keySet()) {
                    ret.add(new SignalInfo(machine, key, true));
                }
            }
        }
        return ret;
    }

    private boolean hasRead(UUID machine, String key) {
        return readSignals.containsKey(machine) && readSignals.get(machine).containsKey(key);
    }

    private boolean hasWrite(UUID machine, String key) {
        return writeSignals.containsKey(machine) && writeSignals.get(machine).containsKey(key);
    }

    public boolean has(UUID machine, String key, boolean isWrite) {
        return isWrite ? hasWrite(machine, key) : hasRead(machine, key);
    }

    public int read(UUID machine, String key) {
        if (!hasRead(machine, key)) {
            LOGGER.warn("{}: Try to read signal failed {}:{}", network, machine, key);
            return 0;
        }
        return readSignals.get(machine).get(key).getAsInt();
    }

    public void write(UUID machine, String key, int val) {
        if (!hasWrite(machine, key)) {
            LOGGER.warn("{}: Try to write signal failed {}:{}", network, machine, key);
            return;
        }
        writeSignals.get(machine).get(key).accept(val);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        readSignals.clear();
        writeSignals.clear();
        subnetMachines.clear();
    }

    @Override
    public void buildSchedulings(SchedulingBuilder builder) {}
}
