package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutocraftModelTest {
    @Test
    void craftKeyShouldUseExactNbtIdentity() {
        var base = CraftKey.item("tinactory:gear", "{quality:1}");
        var same = CraftKey.item("tinactory:gear", "{quality:1}");
        var differentNbt = CraftKey.item("tinactory:gear", "{quality:2}");

        assertEquals(base, same);
        assertThrows(AssertionError.class, () -> assertEquals(base, differentNbt));
    }

    @Test
    void craftPatternShouldKeepOrderedInputsAndOutputs() {
        var ore = new CraftAmount(CraftKey.item("tinactory:ore", ""), 1);
        var plate = new CraftAmount(CraftKey.item("tinactory:plate", ""), 2);
        var slag = new CraftAmount(CraftKey.item("tinactory:slag", ""), 1);
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "crusher"), 2,
            List.of(new TestConstraint("tooling")));

        var pattern = new CraftPattern("tinactory:ore_to_plate", List.of(ore), List.of(plate, slag), requirement);

        assertEquals(List.of(ore), pattern.inputs());
        assertEquals(List.of(plate, slag), pattern.outputs());
        assertEquals(new ResourceLocation("tinactory", "crusher"), pattern.machineRequirement().recipeTypeId());
        assertEquals(2, pattern.machineRequirement().voltageTier());
        assertEquals("tooling", pattern.machineRequirement().constraints().get(0).typeId());
    }

    @Test
    void modelValuesShouldBeImmutable() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "assembler"), 1,
            List.of(new TestConstraint("frame")));
        var pattern = new CraftPattern(
            "tinactory:part",
            List.of(new CraftAmount(CraftKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(CraftKey.item("tinactory:part", ""), 1)),
            requirement);

        assertThrows(UnsupportedOperationException.class, () -> pattern.inputs().add(
            new CraftAmount(CraftKey.item("tinactory:other", ""), 1)));
        assertThrows(IllegalArgumentException.class, () -> new CraftAmount(CraftKey.item("tinactory:invalid", ""), 0));
    }

    private record TestConstraint(String typeId) implements IMachineConstraint {
    }
}
