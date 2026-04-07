package org.shsts.tinactory.unit.core.recipe;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestProcessingIngredient;
import org.shsts.tinactory.unit.fixture.TestProcessingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessingRecipeTest {
    @Test
    void shouldMatchInputsWhenEveryIngredientCanBeConsumed() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingIngredient("ore", 2))
            .input(1, new TestProcessingIngredient("coolant", 1))
            .output(2, new TestProcessingResult("ingot", 1))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(1, PortDirection.INPUT, new TestPort("coolant", 10, 1))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 10, 0));

        assertTrue(recipe.matchInputsForTest(new TestMachine(container), container, 1));
        assertFalse(recipe.matchInputsForTest(new TestMachine(container), container, 3));
    }

    @Test
    void shouldRespectOutputPortLimitWhenCheckingOutputSpace() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingIngredient("ore", 1))
            .output(2, new TestProcessingResult("ingot", 1))
            .output(2, new TestProcessingResult("ingot", 1))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 1, 0, 1));

        assertTrue(recipe.matchOutputsForTest(new TestMachine(container), container, 1, new Random(1L)));
    }

    @Test
    void shouldEmitConsumeAndInsertCallbacksWithProcessingInfo() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingIngredient("ore", 2))
            .input(1, new TestProcessingIngredient("coolant", 1))
            .output(2, new TestProcessingResult("ingot", 3))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(1, PortDirection.INPUT, new TestPort("coolant", 10, 2))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 10, 0));
        var consumed = new ArrayList<ProcessingInfo>();
        var inserted = new ArrayList<TestProcessingResult>();

        recipe.consumeInputs(container, 1, consumed::add);
        recipe.insertOutputs(container, 1, new Random(2L), result -> inserted.add((TestProcessingResult) result));

        assertIterableEquals(List.of(
            new ProcessingInfo(0, new TestProcessingIngredient("ore", 2)),
            new ProcessingInfo(1, new TestProcessingIngredient("coolant", 1))
        ), consumed);
        assertEquals(List.of(new TestProcessingResult("ingot", 3)), inserted);
        assertEquals(2, container.getTestPort(0).stored());
        assertEquals(1, container.getTestPort(1).stored());
        assertEquals(3, container.getTestPort(2).stored());
    }

    @Test
    void shouldRequireEnoughMachineVoltageToCraft() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingIngredient("ore", 1))
            .output(1, new TestProcessingResult("ingot", 1))
            .voltage(120)
            .buildObject();

        assertFalse(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(32)));
        assertTrue(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(120)));
    }

    @Test
    void shouldBypassOutputChecksWhenAutoVoidIsEnabled() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingIngredient("ore", 1))
            .output(1, new TestProcessingResult("ingot", 1))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 1))
            .port(1, PortDirection.OUTPUT, new TestPort("ingot", 1, 1));
        var blockingMachine = new TestMachine(container);
        var autoVoidMachine = new TestMachine(container).autoVoid(true);

        assertFalse(recipe.matchesForTest(blockingMachine, 1, new Random(3L)));
        assertTrue(recipe.matchesForTest(autoVoidMachine, 1, new Random(3L)));
    }

    private static TestRecipe.Builder recipeBuilder() {
        return new TestRecipe.Builder(new ResourceLocation("tinactory", "test_recipe"));
    }

    private static final class TestRecipe extends ProcessingRecipe {
        private TestRecipe(Builder builder) {
            super(builder);
        }

        private boolean matchInputsForTest(TestMachine machine, IContainer container, int parallel) {
            return matchInputs(machine, container, parallel);
        }

        private boolean matchOutputsForTest(TestMachine machine, IContainer container, int parallel,
            Random random) {
            return matchOutputs(machine, container, parallel, random);
        }

        private boolean matchesForTest(TestMachine machine, int parallel, Random random) {
            return canCraft(machine) && machine.container()
                .filter(container -> matchInputs(machine, container, parallel) ||
                    machine.config().getBoolean("void", false))
                .filter(container -> machine.config().getBoolean("void", false) ||
                    matchOutputs(machine, container, parallel, random))
                .isPresent();
        }

        private static final class Builder extends BuilderBase<TestRecipe, Builder> {
            private Builder(ResourceLocation loc) {
                super(null, loc);
                workTicks(20);
                power(8);
            }

            @Override
            protected TestRecipe createObject() {
                return new TestRecipe(this);
            }
        }
    }
}
