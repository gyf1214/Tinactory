package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;

import java.util.List;

public final class TestAutocraftHelper {
    private TestAutocraftHelper() {}

    public static MachineRequirement machineRequirement(String recipeTypeId, int count) {
        return new MachineRequirement(new ResourceLocation(recipeTypeId), count, List.of());
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(id, inputs, outputs, machineRequirement("tinactory:machine", 1));
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, MachineRequirement requirement) {

        return new CraftPattern(id, inputs, outputs, requirement);
    }
}
