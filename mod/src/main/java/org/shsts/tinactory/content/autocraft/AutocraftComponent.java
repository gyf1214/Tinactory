package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;
import org.shsts.tinactory.integration.network.ComponentType;
import org.shsts.tinactory.integration.network.NetworkComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftComponent extends NetworkComponent implements ICpuRuntime {
    private final Map<UUID, CpuState> cpus = new HashMap<>();
    private final PatternRegistryCache patterns = new PatternRegistryCache();

    public AutocraftComponent(ComponentType<AutocraftComponent> type, INetwork network) {
        super(type, network);
    }

    @Override
    public void registerCpu(IMachine machine, IAutocraftService service) {
        cpus.put(machine.uuid(), new CpuState(machine, service));
    }

    @Override
    public void unregisterCpu(UUID cpuId) {
        cpus.remove(cpuId);
    }

    @Override
    public List<UUID> listVisibleCpus() {
        return cpus.values().stream()
            .map(CpuState::cpuId)
            .sorted()
            .toList();
    }

    @Override
    public Optional<IMachine> findVisibleCpuMachine(UUID cpuId) {
        var cpu = cpus.get(cpuId);
        if (cpu == null) {
            return Optional.empty();
        }
        return Optional.of(cpu.machine());
    }

    @Override
    public Optional<IAutocraftService> findVisibleService(UUID cpuId) {
        var cpu = cpus.get(cpuId);
        if (cpu == null) {
            return Optional.empty();
        }
        return Optional.of(cpu.service());
    }

    public IPatternRepository patternRepository() {
        return patterns;
    }

    @Override
    public void onDisconnect() {
        cpus.clear();
        patterns.clear();
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {}

    private record CpuState(IMachine machine, IAutocraftService service) {
        private UUID cpuId() {
            return machine.uuid();
        }
    }
}
