package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftStep(
    String stepId,
    CraftPattern pattern,
    long runs,
    List<CraftAmount> requiredIntermediateOutputs,
    List<CraftAmount> requiredFinalOutputs
) {
    public CraftStep {
        if (stepId.isBlank()) {
            throw new IllegalArgumentException("stepId must not be blank");
        }
        if (runs <= 0L) {
            throw new IllegalArgumentException("runs must be positive");
        }
        requiredIntermediateOutputs = List.copyOf(requiredIntermediateOutputs);
        requiredFinalOutputs = List.copyOf(requiredFinalOutputs);
    }

    public CraftStep(String stepId, CraftPattern pattern, long runs, List<CraftAmount> requiredOutputs) {
        this(stepId, pattern, runs, List.of(), requiredOutputs);
    }

    public CraftStep(String stepId, CraftPattern pattern, long runs) {
        this(stepId, pattern, runs, List.of(), pattern.outputs());
    }

    public List<CraftAmount> requiredOutputs() {
        var outputs = new ArrayList<CraftAmount>(requiredIntermediateOutputs.size() + requiredFinalOutputs.size());
        outputs.addAll(requiredIntermediateOutputs);
        outputs.addAll(requiredFinalOutputs);
        return List.copyOf(outputs);
    }
}
