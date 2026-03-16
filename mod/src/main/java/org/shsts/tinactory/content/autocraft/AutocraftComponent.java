package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
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
public class AutocraftComponent extends NetworkComponent {
    private final Map<UUID, AutocraftCpuState> autocraftCpus = new HashMap<>();
    private final PatternRegistryCache patternRepository = new PatternRegistryCache();

    public AutocraftComponent(ComponentType<AutocraftComponent> type, INetwork network) {
        super(type, network);
    }

    public void registerCpu(IMachine machine, BlockPos subnet, IAutocraftService service) {
        autocraftCpus.put(machine.uuid(), new AutocraftCpuState(machine, subnet, service));
    }

    public void unregisterCpu(UUID cpuId) {
        autocraftCpus.remove(cpuId);
    }

    public boolean isCpuRegistered(UUID cpuId) {
        return autocraftCpus.containsKey(cpuId);
    }

    public List<UUID> listVisibleCpus() {
        return autocraftCpus.values().stream()
            .map(AutocraftCpuState::cpuId)
            .sorted()
            .toList();
    }

    public List<UUID> listAvailableCpus() {
        return autocraftCpus.values().stream()
            .filter(cpu -> !cpu.service().isBusy())
            .map(AutocraftCpuState::cpuId)
            .sorted()
            .toList();
    }

    public Optional<IAutocraftService> findVisibleService(UUID cpuId) {
        var cpu = autocraftCpus.get(cpuId);
        if (cpu == null) {
            return Optional.empty();
        }
        return Optional.of(cpu.service());
    }

    public IPatternRepository patternRepository() {
        return patternRepository;
    }

    @Override
    public void onDisconnect() {
        autocraftCpus.clear();
        patternRepository.clear();
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {}

    private record AutocraftCpuState(IMachine machine, BlockPos subnet, IAutocraftService service) {
        private UUID cpuId() {
            return machine.uuid();
        }
    }
}
