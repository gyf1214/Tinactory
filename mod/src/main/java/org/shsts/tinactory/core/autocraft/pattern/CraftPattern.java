package org.shsts.tinactory.core.autocraft.pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPattern(
    UUID patternUuid,
    List<CraftAmount> inputs,
    List<CraftAmount> outputs,
    List<IMachineConstraint> constraints) {
    public CraftPattern {
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        constraints = List.copyOf(constraints);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must not be empty");
        }
    }

    public CraftPattern withUuid(UUID uuid) {
        return new CraftPattern(uuid, inputs, outputs, constraints);
    }
}
