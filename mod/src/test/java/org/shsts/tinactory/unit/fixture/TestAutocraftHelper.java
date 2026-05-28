package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class TestAutocraftHelper {
    private TestAutocraftHelper() {}

    public static List<IMachineConstraint> constraints(String recipeTypeId, int voltageTier) {
        return List.of(
            new RecipeTypeConstraint(new ResourceLocation(recipeTypeId)),
            new VoltageConstraint(voltageTier));
    }

    public static CraftPattern pattern(UUID id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(id, inputs, outputs, constraints("tinactory:machine", 1));
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(uuid(id), inputs, outputs);
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, List<IMachineConstraint> constraints) {

        return pattern(uuid(id), inputs, outputs, constraints);
    }

    public static CraftPattern pattern(UUID id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, List<IMachineConstraint> constraints) {

        return new CraftPattern(id, inputs, outputs, constraints);
    }

    public static UUID uuid(String id) {
        return UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
    }
}
