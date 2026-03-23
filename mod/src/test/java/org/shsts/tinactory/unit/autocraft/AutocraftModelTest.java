package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.unit.fixture.TestIngredientKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutocraftModelTest {
    @Test
    void craftKeyShouldUseExactNbtIdentity() {
        var base = TestIngredientKey.item("tinactory:gear", "{quality:1}");
        var same = TestIngredientKey.item("tinactory:gear", "{quality:1}");
        var differentNbt = TestIngredientKey.item("tinactory:gear", "{quality:2}");

        assertEquals(base, same);
        assertThrows(AssertionError.class, () -> assertEquals(base, differentNbt));
    }

    @Test
    void craftPatternShouldKeepOrderedInputsAndOutputs() {
        var ore = new CraftAmount(TestIngredientKey.item("tinactory:ore", ""), 1);
        var plate = new CraftAmount(TestIngredientKey.item("tinactory:plate", ""), 2);
        var slag = new CraftAmount(TestIngredientKey.item("tinactory:slag", ""), 1);
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
            List.of(new CraftAmount(TestIngredientKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestIngredientKey.item("tinactory:part", ""), 1)),
            requirement);

        assertThrows(UnsupportedOperationException.class, () -> pattern.inputs().add(
            new CraftAmount(TestIngredientKey.item("tinactory:other", ""), 1)));
        assertThrows(IllegalArgumentException.class,
            () -> new CraftAmount(TestIngredientKey.item("tinactory:invalid", ""), 0));
    }

    @Test
    void portConstraintShouldValidateDirectionAndIndices() {
        var constraint = new PortConstraint(PortDirection.INPUT, 0, 2);
        assertEquals(PortDirection.INPUT, constraint.direction());
        assertEquals(0, constraint.slotIndex());
        assertEquals(2, constraint.portIndex());
        assertEquals("tinactory:port", constraint.typeId());

        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.INPUT, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.INPUT, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.NONE, 0, null));
    }

    @Test
    void slotScopedConstraintsShouldDisambiguateByDirectionAndSlotIndex() {
        var input0 = new PortConstraint(PortDirection.INPUT, 0, 1);
        var input1 = new PortConstraint(PortDirection.INPUT, 1, 1);
        var output0 = new PortConstraint(PortDirection.OUTPUT, 0, 3);
        var output1 = new PortConstraint(PortDirection.OUTPUT, 1, 3);

        assertThrows(AssertionError.class, () -> assertEquals(input0, input1));
        assertThrows(AssertionError.class, () -> assertEquals(output0, output1));
    }

    private record TestConstraint(String typeId) implements IMachineConstraint {
    }
}
