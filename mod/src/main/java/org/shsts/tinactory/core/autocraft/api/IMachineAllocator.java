package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineAllocator {
    default Optional<IMachineLease> allocate(CraftStep step) {
        return allocate(step, Set.of());
    }

    Optional<IMachineLease> allocate(CraftStep step, Set<UUID> excludedMachineIds);

    default Optional<IMachineLease> allocate(CraftStep step, UUID machineId) {
        var lease = allocate(step, Set.of());
        if (lease.isEmpty()) {
            return Optional.empty();
        }
        if (lease.get().machineId().equals(machineId)) {
            return lease;
        }
        lease.get().release();
        return Optional.empty();
    }
}
