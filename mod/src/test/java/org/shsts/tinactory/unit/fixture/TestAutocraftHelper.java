package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;

import java.util.List;

public final class TestAutocraftHelper {
    private TestAutocraftHelper() {}

    public static List<IMachineConstraint> constraints(String recipeTypeId, int voltageTier) {
        return List.of(new RecipeTypeConstraint(new ResourceLocation(recipeTypeId)), new VoltageConstraint(voltageTier));
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(id, inputs, outputs, constraints("tinactory:machine", 1));
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, List<IMachineConstraint> constraints) {

        return new CraftPattern(id, inputs, outputs, constraints);
    }
}
