package org.shsts.tinactory.unit.machine;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestEntry;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestRecipe;
import org.shsts.tinactory.unit.fixture.TestRecipeManager;
import org.shsts.tinactory.unit.fixture.TestRecipeType;
import org.shsts.tinactory.unit.fixture.TestResult;
import org.shsts.tinactory.unit.fixture.TestStack;
import org.shsts.tinycorelib.api.core.ILoc;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.api.logistics.PortDirection.INPUT;
import static org.shsts.tinactory.api.logistics.PortDirection.OUTPUT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.input;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.output;
import static org.shsts.tinactory.unit.fixture.TestRegistry.TEST_REGISTRY;

class ProcessingMachineTest {
    private static final int INPUT_PORT = 0;
    private static final int OUTPUT_PORT = 1;
    private static final TestRecipeType<TestRecipe> RECIPE_TYPE =
        new TestRecipeType<>("test_machine", TestRecipe.class);
    private static final TestRecipeType<MarkerRecipe> MARKER_TYPE =
        new TestRecipeType<>("marker", MarkerRecipe.class);

    @Test
    void shouldUseScaledPreviewForGenericResultsWhenBuildingOutputInfo() {
        var recipe = new TestRecipe(List.of(), List.of(output(2, "dust", 2)), 20, 1, 8);
        var info = new ArrayList<ProcessingInfo>();

        new TestProcessingMachine().addOutputInfoForTest(recipe, 3, info::add);

        assertEquals(List.of(new ProcessingInfo(2, new TestResult("dust", 6))), info);
    }

    @Test
    void shouldBuildRecipeBookItemsFromInjectedRecipeManager() {
        var machine = new TestMachine(new TestContainer());
        var marker = marker("group", 0);
        var alpha = recipe("alpha", 0);
        var beta = recipe("beta", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager()
            .add(MARKER_TYPE, marker)
            .add(RECIPE_TYPE, beta)
            .add(RECIPE_TYPE, alpha));

        var items = processor.recipeBookItems(machine).getValue();

        assertEquals(List.of(alpha.loc(), beta.loc(), marker.loc()), items.stream().map(ILoc::loc).toList());
        assertFalse(items.get(0).isMarker());
        assertFalse(items.get(1).isMarker());
        assertTrue(items.get(2).isMarker());
    }

    @Test
    void shouldAllowMarkerTargetsOnServerAndSkipCanCraftForProcessingTargetsOnClient() {
        var machine = new TestMachine(new TestContainer()).electricVoltage(16);
        var marker = marker("server_marker", 0);
        var blockedMarker = marker("blocked_marker", 32);
        var blockedRecipe = recipe("blocked_processing", 32);
        var processor = new TestProcessingMachine(new TestRecipeManager()
            .add(MARKER_TYPE, marker)
            .add(MARKER_TYPE, blockedMarker)
            .add(RECIPE_TYPE, blockedRecipe));

        assertTrue(processor.allowTargetRecipe(false, marker.loc(), machine));
        assertFalse(processor.allowTargetRecipe(false, blockedMarker.loc(), machine));
        assertFalse(processor.allowTargetRecipe(false, blockedRecipe.loc(), machine));
        assertTrue(processor.allowTargetRecipe(true, blockedRecipe.loc(), machine));
    }

    @Test
    void shouldPreserveFilterTargetAcrossSerialization() {
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new TestPort("dust", 16, 0)));
        var target = recipe("targeted", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager().add(RECIPE_TYPE, target));

        var recipe = processor.newRecipe(machine, target.loc()).orElseThrow();
        processor.onWorkBegin(recipe, machine, 1, $ -> {});
        var saved = processor.serializeNBT(TEST_REGISTRY);

        var restored = new TestProcessingMachine(new TestRecipeManager().add(RECIPE_TYPE, target));
        restored.deserializeNBT(TEST_REGISTRY, saved);
        restored.onWorkContinue(recipe, machine);
        var restoredTag = restored.serializeNBT(TEST_REGISTRY);

        assertEquals(target.loc().toString(), saved.getString("filterRecipe"));
        assertEquals(target.loc().toString(), restoredTag.getString("filterRecipe"));
    }

    @Test
    void shouldSelectRecipesThroughInjectedTargetLookups() {
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new TestPort("dust", 16, 0)));
        var marker = marker("ore", 0);
        var matching = recipe("ore/matching", 0);
        var other = recipe("other", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager()
            .add(MARKER_TYPE, marker)
            .add(RECIPE_TYPE, matching)
            .add(RECIPE_TYPE, other));

        assertEquals(Optional.of(matching.loc()),
            processor.newRecipe(machine, marker.loc()).map(ILoc::loc));
        assertEquals(Optional.of(other.loc()), processor.newRecipe(machine, other.loc()).map(ILoc::loc));
    }

    @Test
    void shouldLeaveInputFiltersUnchangedWhenTargetRecipeDoesNotResolve() {
        var input = new TestPort("ore", 16, 0);
        var machine = new TestMachine(new TestContainer().port(INPUT_PORT, INPUT, input));
        var processor = new TestProcessingMachine();

        processor.setTargetRecipe(modLoc("missing"), machine);

        assertTrue(input.acceptInput(TestStack.item("ore", 1)));
    }

    @Test
    void shouldLeaveMarkerInputFiltersUnsetWhenMarkerHasNoInputs() {
        var input = new TestPort("ore", 16, 0);
        var machine = new TestMachine(new TestContainer().port(INPUT_PORT, INPUT, input));
        var marker = marker("ore", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager().add(MARKER_TYPE, marker));

        processor.setTargetRecipe(marker.loc(), machine);

        assertTrue(input.acceptInput(TestStack.item("ore", 1)));
    }

    @Test
    void shouldLeaveMarkerOutputFiltersUnsetWhenMarkerHasNoMarkerOutputs() {
        var output = new TestPort("dust", 16, 0);
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, output));
        var marker = marker("ore", 0);
        var matching = recipe("ore/matching", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager()
            .add(MARKER_TYPE, marker)
            .add(RECIPE_TYPE, matching));

        var resolved = processor.newRecipe(machine, marker.loc());

        assertEquals(Optional.of(matching.loc()), resolved.map(ILoc::loc));
        assertTrue(output.acceptInput(TestStack.item("dust", 1)));
    }

    @Test
    void shouldReturnEmptyWhenMarkerTargetHasNoMatchingConcreteRecipe() {
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new TestPort("dust", 16, 0)));
        var marker = marker("ore", 0);
        var processor = new TestProcessingMachine(new TestRecipeManager()
            .add(MARKER_TYPE, marker)
            .add(RECIPE_TYPE, recipe("other", 0)));

        assertTrue(processor.newRecipe(machine, marker.loc()).isEmpty());
    }

    @Test
    void shouldCalculateParallelUsingBinarySearchLimits() {
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 3))
            .port(OUTPUT_PORT, OUTPUT, new TestPort("dust", 16, 0)));
        var recipe = recipe("parallel", 0);
        var processor = new TestProcessingMachine();

        assertEquals(1, processor.calculateParallelForTest(recipe.get(), machine, 1));
        assertEquals(3, processor.calculateParallelForTest(recipe.get(), machine, 4));
    }

    @Test
    void shouldCalculateLowVoltageAndOverclockFactors() {
        var recipe = recipe("factors", 8);
        var lowVoltageMachine = new TestMachine(new TestContainer()).electricVoltage(8);
        var overclockedMachine = new TestMachine(new TestContainer()).electricVoltage(128);
        var processor = new TestProcessingMachine();

        processor.calculateFactorsForTest(recipe.get(), lowVoltageMachine, 1);
        assertEquals(ProcessingMachine.PROGRESS_PER_TICK, processor.onWorkProgress(recipe, 1d));
        assertEquals(8d, processor.powerCons(recipe));

        processor.calculateFactorsForTest(recipe.get(), overclockedMachine, 1);
        assertEquals(ProcessingMachine.PROGRESS_PER_TICK * 4L, processor.onWorkProgress(recipe, 1d));
        assertEquals(128d, processor.powerCons(recipe));
    }

    @Test
    void shouldClearSerializedFilterRecipeWhenItCanNoLongerResolve() {
        var machine = new TestMachine(new TestContainer()
            .port(INPUT_PORT, INPUT, new TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new TestPort("dust", 16, 0)));
        var target = recipe("targeted", 0);
        var original = new TestProcessingMachine(new TestRecipeManager().add(RECIPE_TYPE, target));
        var recipe = original.newRecipe(machine, target.loc()).orElseThrow();
        original.onWorkBegin(recipe, machine, 1, $ -> {});
        var saved = original.serializeNBT(TEST_REGISTRY);

        var restored = new TestProcessingMachine(new TestRecipeManager());
        restored.deserializeNBT(TEST_REGISTRY, saved);
        restored.onWorkContinue(recipe, machine);

        assertFalse(restored.serializeNBT(TEST_REGISTRY).contains("filterRecipe"));
    }

    private static IEntry<TestRecipe> recipe(String path, long voltage) {
        return new TestEntry<>(modLoc(path), new TestRecipe(
            List.of(input(INPUT_PORT, "ore", 1)),
            List.of(output(OUTPUT_PORT, "dust", 1)),
            20, voltage, 8));
    }

    private static IEntry<MarkerRecipe> marker(String path, long voltage) {
        return new TestEntry<>(modLoc(path), new MarkerRecipe(
            List.of(), List.of(), RECIPE_TYPE.loc(), "ore",
            false, Optional.empty(), Optional.empty(), List.of()));
    }

    private static final class TestProcessingMachine extends ProcessingMachine<TestRecipe> {
        private TestProcessingMachine() {
            this(new TestRecipeManager());
        }

        private TestProcessingMachine(TestRecipeManager recipeManager) {
            super(RECIPE_TYPE, () -> recipeManager, MARKER_TYPE);
        }

        private void addOutputInfoForTest(TestRecipe recipe, int parallel, Consumer<ProcessingInfo> info) {
            addOutputInfo(recipe, parallel, info);
        }

        private int calculateParallelForTest(TestRecipe recipe, TestMachine machine, int maxParallel) {
            return calculateParallel(recipe, machine, maxParallel);
        }

        private void calculateFactorsForTest(TestRecipe recipe, TestMachine machine, int parallel) {
            calculateFactors(recipe, machine, parallel);
        }
    }
}
