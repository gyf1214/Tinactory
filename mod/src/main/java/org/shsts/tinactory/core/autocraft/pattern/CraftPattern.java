package org.shsts.tinactory.core.autocraft.pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPattern(
    String patternId,
    List<CraftAmount> inputs,
    List<CraftAmount> outputs,
    List<IMachineConstraint> constraints) {
    public CraftPattern {
        if (patternId.isBlank()) {
            throw new IllegalArgumentException("patternId must not be blank");
        }
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        constraints = List.copyOf(constraints);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must not be empty");
        }
    }
}
