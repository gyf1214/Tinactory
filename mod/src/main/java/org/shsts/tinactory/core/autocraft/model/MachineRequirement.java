package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MachineRequirement(String machineType, int voltageTier, List<IMachineConstraint> constraints) {
    public MachineRequirement {
        if (machineType.isBlank()) {
            throw new IllegalArgumentException("machineType must not be blank");
        }
        if (voltageTier < 0) {
            throw new IllegalArgumentException("voltageTier must not be negative");
        }
        constraints = List.copyOf(constraints);
    }
}
