package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestMachineConstraint;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftModelTest {
    @Test
    void craftKeyShouldUseExactNbtIdentity() {
        var base = TestStackKey.item("tinactory:gear", "{quality:1}");
        var same = TestStackKey.item("tinactory:gear", "{quality:1}");
        var differentNbt = TestStackKey.item("tinactory:gear", "{quality:2}");

        assertEquals(base, same);
        assertThrows(AssertionError.class, () -> assertEquals(base, differentNbt));
    }

    @Test
    void craftPatternShouldKeepOrderedInputsAndOutputs() {
        var ore = new CraftAmount(TestStackKey.item("tinactory:ore", ""), 1);
        var plate = new CraftAmount(TestStackKey.item("tinactory:plate", ""), 2);
        var slag = new CraftAmount(TestStackKey.item("tinactory:slag", ""), 1);
        List<IMachineConstraint> constraints = List.of(new TestMachineConstraint("tooling"));

        var pattern = new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:ore_to_plate"),
            List.of(ore),
            List.of(plate, slag),
            constraints);

        assertEquals(List.of(ore), pattern.inputs());
        assertEquals(List.of(plate, slag), pattern.outputs());
        assertEquals(new TestMachineConstraint("tooling"), pattern.constraints().get(0));
    }

    @Test
    void modelValuesShouldBeImmutable() {
        var pattern = new CraftPattern(
            TestAutocraftHelper.uuid("tinactory:part"),
            List.of(new CraftAmount(TestStackKey.item("tinactory:ingot", ""), 2)),
            List.of(new CraftAmount(TestStackKey.item("tinactory:part", ""), 1)),
            List.of(new TestMachineConstraint("frame")));

        assertThrows(UnsupportedOperationException.class, () -> pattern.inputs().add(
            new CraftAmount(TestStackKey.item("tinactory:other", ""), 1)));
        assertThrows(UnsupportedOperationException.class,
            () -> pattern.constraints().add(new TestMachineConstraint("other")));
        assertThrows(IllegalArgumentException.class,
            () -> new CraftAmount(TestStackKey.item("tinactory:invalid", ""), 0));
    }

    @Test
    void portConstraintShouldValidateDirectionAndIndices() {
        var constraint = new PortConstraint(PortDirection.INPUT, 0, 2);
        assertEquals(PortDirection.INPUT, constraint.direction());
        assertEquals(0, constraint.index());
        assertEquals(2, constraint.port());
        assertEquals("tinactory:port", constraint.typeId());

        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.INPUT, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.INPUT, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new PortConstraint(PortDirection.NONE, 0, 0));
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

    @Test
    void machineConstraintDefaultsShouldAcceptMachinesAndRoutesWithoutLeaseSideEffects() {
        IMachineConstraint constraint = new TestMachineConstraint("payload");
        var machine = new TestMachine(null);
        var key = TestStackKey.item("tinactory:ingot", "");

        assertTrue(constraint.matches(machine, Voltage.LV));
        assertTrue(constraint.matchesRoute(PortDirection.INPUT, 0, key, 2, 1, PortType.ITEM));
        assertTrue(constraint.configureLease(machine).isEmpty());
    }

    @Test
    void recipeTypeConstraintShouldMatchSupportedMachineProcessorTypes() {
        var assembler = new ResourceLocation("tinactory", "assembler");
        var mixer = new ResourceLocation("tinactory", "mixer");
        var constraint = new RecipeTypeConstraint(assembler);
        var machine = new TestMachine(null).supportsRecipeType(assembler);

        assertEquals(assembler, constraint.recipeTypeId());
        assertEquals("tinactory:recipe_type", constraint.typeId());
        assertTrue(constraint.matches(machine, Voltage.LV));
        assertFalse(new RecipeTypeConstraint(mixer).matches(machine, Voltage.LV));
        assertFalse(constraint.matches(new TestMachine(null), Voltage.LV));
    }

    @Test
    void voltageConstraintShouldMatchMinimumMachineVoltage() {
        var constraint = new VoltageConstraint(Voltage.MV.rank);

        assertEquals(Voltage.MV.rank, constraint.tier());
        assertEquals("tinactory:voltage", constraint.typeId());
        assertTrue(constraint.matches(new TestMachine(null), Voltage.HV));
        assertTrue(constraint.matches(new TestMachine(null), Voltage.MV));
        assertFalse(constraint.matches(new TestMachine(null), Voltage.LV));
        assertThrows(IllegalArgumentException.class, () -> new VoltageConstraint(-1));
    }

    @Test
    void portConstraintShouldMatchOnlyTheBoundRoute() {
        var constraint = new PortConstraint(PortDirection.INPUT, 0, 2);
        var key = TestStackKey.item("tinactory:ingot", "");

        assertTrue(constraint.matchesRoute(PortDirection.INPUT, 0, key, 4, 2, PortType.ITEM));
        assertFalse(constraint.matchesRoute(PortDirection.INPUT, 0, key, 4, 3, PortType.ITEM));
        assertTrue(constraint.matchesRoute(PortDirection.INPUT, 1, key, 4, 3, PortType.ITEM));
        assertTrue(constraint.matchesRoute(PortDirection.OUTPUT, 0, key, 4, 3, PortType.ITEM));
    }

    @Test
    void portConstraintShouldNotBlockDifferentKeysAmountsOrPortTypesOnOtherSlots() {
        var constraint = new PortConstraint(PortDirection.OUTPUT, 1, 2);

        assertTrue(constraint.matchesRoute(PortDirection.OUTPUT, 0,
            TestStackKey.item("tinactory:gear", ""), 1, 5, PortType.ITEM));
        assertTrue(constraint.matchesRoute(PortDirection.INPUT, 1,
            TestStackKey.item("tinactory:gear", ""), 1, 5, PortType.ITEM));
        assertTrue(constraint.matchesRoute(PortDirection.OUTPUT, 1,
            TestStackKey.fluid("tinactory:steam", ""), 1000, 2, PortType.FLUID));
    }

    @Test
    void targetRecipeConstraintShouldValidateRecipeId() {
        var recipeId = new ResourceLocation("tinactory", "assembler/circuit");
        var constraint = new TargetRecipeConstraint(recipeId);

        assertEquals(recipeId, constraint.recipeId());
        assertEquals("tinactory:target_recipe", constraint.typeId());
        assertThrows(IllegalArgumentException.class,
            () -> new TargetRecipeConstraint(new ResourceLocation("tinactory", "")));
    }

    @Test
    void targetRecipeConstraintShouldMatchAndRestoreMachineConfig() {
        var recipeId = new ResourceLocation("tinactory", "assembler/circuit");
        var previous = new ResourceLocation("tinactory", "assembler/previous");
        var constraint = new TargetRecipeConstraint(recipeId);
        var machine = new TestMachine(null).supportsRecipeType(new ResourceLocation("tinactory", "assembler"))
            .targetRecipe(previous);

        assertTrue(constraint.matches(machine, Voltage.LV));
        var restore = constraint.configureLease(machine).orElseThrow();
        assertEquals(recipeId.toString(), machine.targetRecipe().orElseThrow());

        restore.run();

        assertEquals(previous.toString(), machine.targetRecipe().orElseThrow());
    }

    @Test
    void targetRecipeConstraintRestoreCallbacksShouldNestCleanlyWhenGuardedByLeaseRelease() {
        var firstRecipe = new ResourceLocation("tinactory", "assembler/first");
        var secondRecipe = new ResourceLocation("tinactory", "assembler/second");
        var machine = new TestMachine(null).supportsRecipeType(new ResourceLocation("tinactory", "assembler"));
        var firstRestore = new TargetRecipeConstraint(firstRecipe).configureLease(machine).orElseThrow();
        var secondRestore = new TargetRecipeConstraint(secondRecipe).configureLease(machine).orElseThrow();

        assertEquals(secondRecipe.toString(), machine.targetRecipe().orElseThrow());

        secondRestore.run();
        firstRestore.run();

        assertTrue(machine.targetRecipe().isEmpty());
    }
}
