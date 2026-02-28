package org.shsts.tinactory.content.logistics;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJob;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.integration.AutocraftSubmitErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftSubmitResult;
import org.shsts.tinactory.core.autocraft.integration.NetworkPatternCell;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.integration.network.ComponentType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final SetMultimap<BlockPos, PortKey> subnetPorts = HashMultimap.create();
    private final Set<PortKey> storagePorts = new HashSet<>();
    private final Set<PortKey> globalPorts = new HashSet<>();
    private final Map<UUID, AutocraftCpuState> autocraftCpus = new HashMap<>();
    private final List<NetworkPatternCell> patternCells = new ArrayList<>();

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

    public void registerPort(IMachine machine, int index, IPort<?> port,
        boolean isGlobal) {
        registerPortInSubnet(machine, index, port, getMachineSubnet(machine),
            isGlobal, -1);
    }

    public void registerStoragePort(IMachine machine, int index, IPort<?> port,
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

    public Collection<IPort<?>> getStoragePorts() {
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

    public void registerAutocraftCpu(IMachine machine, BlockPos subnet, AutocraftJobService service) {
        autocraftCpus.put(machine.uuid(), new AutocraftCpuState(machine, subnet, service));
    }

    public void unregisterAutocraftCpu(UUID cpuId) {
        autocraftCpus.remove(cpuId);
    }

    public boolean isAutocraftCpuRegistered(UUID cpuId) {
        return autocraftCpus.containsKey(cpuId);
    }

    public List<UUID> listVisibleAutocraftCpus(BlockPos subnet) {
        return autocraftCpus.values().stream()
            .filter(cpu -> cpu.subnet().equals(subnet))
            .map(AutocraftCpuState::cpuId)
            .sorted()
            .toList();
    }

    public List<UUID> listAvailableAutocraftCpus(BlockPos subnet) {
        return autocraftCpus.values().stream()
            .filter(cpu -> cpu.subnet().equals(subnet) && !cpu.service().isBusy())
            .map(AutocraftCpuState::cpuId)
            .sorted()
            .toList();
    }

    public Optional<AutocraftJobService> findVisibleAutocraftService(BlockPos subnet, UUID cpuId) {
        var cpu = autocraftCpus.get(cpuId);
        if (cpu == null || !cpu.subnet().equals(subnet)) {
            return Optional.empty();
        }
        return Optional.of(cpu.service());
    }

    public AutocraftSubmitResult submitAutocraft(BlockPos subnet, UUID cpuId, List<CraftAmount> targets) {
        if (targets.isEmpty()) {
            return AutocraftSubmitResult.failure(AutocraftSubmitErrorCode.INVALID_REQUEST);
        }
        var cpu = autocraftCpus.get(cpuId);
        if (cpu == null) {
            return AutocraftSubmitResult.failure(AutocraftSubmitErrorCode.CPU_OFFLINE);
        }
        if (!cpu.subnet().equals(subnet)) {
            return AutocraftSubmitResult.failure(AutocraftSubmitErrorCode.CPU_NOT_VISIBLE);
        }
        var service = cpu.service();
        if (service.isBusy()) {
            return AutocraftSubmitResult.failure(AutocraftSubmitErrorCode.CPU_BUSY);
        }
        return AutocraftSubmitResult.success(service.submit(targets));
    }

    public Optional<AutocraftJob> findAutocraftJob(UUID id) {
        for (var cpu : autocraftCpus.values()) {
            var ret = cpu.service().findJob(id);
            if (ret.isPresent()) {
                return ret;
            }
        }
        return Optional.empty();
    }

    public List<AutocraftJob> listAutocraftJobs() {
        return autocraftCpus.values().stream()
            .flatMap(cpu -> cpu.service().listJobs().stream())
            .toList();
    }

    public boolean cancelAutocraft(UUID id) {
        for (var cpu : autocraftCpus.values()) {
            if (cpu.service().cancel(id)) {
                return true;
            }
        }
        return false;
    }

    public void tickAutocraftJobs() {
        for (var cpu : autocraftCpus.values()) {
            cpu.service().tick();
        }
    }

    public void registerPatternCell(NetworkPatternCell cell) {
        patternCells.removeIf(existing ->
            existing.machineId().equals(cell.machineId()) && existing.slotIndex() == cell.slotIndex());
        patternCells.add(cell);
    }

    public void unregisterPatternCells(UUID machineId) {
        patternCells.removeIf(cell -> cell.machineId().equals(machineId));
    }

    public List<CraftPattern> listVisiblePatterns() {
        var ordered = patternCells.stream()
            .sorted(NetworkPatternCell.ORDER)
            .toList();

        var out = new ArrayList<CraftPattern>();
        var dedup = new HashSet<String>();
        for (var cell : ordered) {
            for (var pattern : cell.patterns()) {
                if (dedup.add(pattern.patternId())) {
                    out.add(pattern);
                } else {
                    LOGGER.warn("duplicate autocraft pattern id {}, keep first-seen", pattern.patternId());
                }
            }
        }
        return out;
    }

    public boolean writePattern(CraftPattern pattern) {
        var ordered = patternCells.stream()
            .sorted(NetworkPatternCell.ORDER)
            .toList();
        for (var cell : ordered) {
            if (cell.insert(pattern)) {
                return true;
            }
        }
        return false;
    }

    public boolean removePattern(String patternId) {
        var ordered = patternCells.stream()
            .sorted(NetworkPatternCell.ORDER)
            .toList();
        for (var cell : ordered) {
            if (cell.remove(patternId)) {
                return true;
            }
        }
        return false;
    }

    public boolean updatePattern(CraftPattern pattern) {
        removePattern(pattern.patternId());
        return writePattern(pattern);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        ports.clear();
        subnetPorts.clear();
        globalPorts.clear();
        storagePorts.clear();
        autocraftCpus.clear();
        patternCells.clear();
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {
        // Autocraft CPUs own runtime ticking via machine lifecycle scheduling.
    }

    private record AutocraftCpuState(IMachine machine, BlockPos subnet, AutocraftJobService service) {
        private UUID cpuId() {
            return machine.uuid();
        }
    }
}
