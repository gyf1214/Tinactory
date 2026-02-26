package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPlan(List<CraftStep> steps) {
    public CraftPlan {
        steps = List.copyOf(steps);
    }
}
