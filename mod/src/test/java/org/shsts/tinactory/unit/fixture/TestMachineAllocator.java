package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class TestMachineAllocator implements IMachineAllocator {
    private final List<IMachineLease> leases;
    private int next;
    private int normalCalls;
    private int targetedCalls;

    private TestMachineAllocator(List<IMachineLease> leases) {
        this.leases = List.copyOf(leases);
    }

    public static TestMachineAllocator single(IMachineLease lease) {
        return new TestMachineAllocator(List.of(lease));
    }

    public static TestMachineAllocator sequence(List<? extends IMachineLease> leases) {
        return new TestMachineAllocator(List.copyOf(leases));
    }

    public static TestMachineAllocator empty() {
        return new TestMachineAllocator(List.of());
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step, Set<UUID> excludedMachineIds) {
        normalCalls++;
        while (next < leases.size()) {
            var lease = leases.get(next++);
            if (!excludedMachineIds.contains(lease.machineId())) {
                return Optional.of(lease);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step, UUID machineId) {
        targetedCalls++;
        return leases.stream()
            .filter(lease -> lease.machineId().equals(machineId))
            .findFirst();
    }

    public int normalCalls() {
        return normalCalls;
    }

    public int targetedCalls() {
        return targetedCalls;
    }
}
