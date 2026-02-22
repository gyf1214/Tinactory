package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MachineRequirement(ResourceLocation recipeTypeId, int voltageTier, List<IMachineConstraint> constraints) {
    public MachineRequirement {
        if (recipeTypeId.getPath().isBlank()) {
            throw new IllegalArgumentException("recipeTypeId must not be blank");
        }
        if (voltageTier < 0) {
            throw new IllegalArgumentException("voltageTier must not be negative");
        }
        constraints = List.copyOf(constraints);
    }
}
