package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPattern(
    String patternId,
    List<CraftAmount> inputs,
    List<CraftAmount> outputs,
    MachineRequirement machineRequirement) {
    public CraftPattern {
        if (patternId.isBlank()) {
            throw new IllegalArgumentException("patternId must not be blank");
        }
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must not be empty");
        }
    }
}
