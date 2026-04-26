package org.shsts.tinactory.unit.machine;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestResult;
import org.shsts.tinactory.unit.fixture.TestRecipeManager;
import org.shsts.tinactory.unit.fixture.TestRecipeType;
import org.shsts.tinactory.unit.fixture.TestStack;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.api.logistics.PortDirection.INPUT;
import static org.shsts.tinactory.api.logistics.PortDirection.OUTPUT;

class ProcessingMachineTest {
    private static final int INPUT_PORT = 0;
    private static final int OUTPUT_PORT = 1;
    private static final TestRecipeType<TestRecipe.Builder> RECIPE_TYPE =
        new TestRecipeType<>("test_machine", TestRecipe.class, TestRecipe.Builder::new);
    private static final TestRecipeType<MarkerRecipe.Builder> MARKER_TYPE =
        new TestRecipeType<>("marker", MarkerRecipe.class, MarkerRecipe.Builder::new);

    @Test
    void shouldUseScaledPreviewForGenericResultsWhenBuildingOutputInfo() {
        var recipe = new TestRecipe.Builder(RECIPE_TYPE, new ResourceLocation("tinactory", "test_recipe"))
            .output(2, new TestResult("dust", 2))
            .workTicks(20)
            .power(8)
            .buildObject();
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

        assertEquals(List.of(alpha.loc(), beta.loc(), marker.loc()), items.stream().map($ -> $.loc()).toList());
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
        var saved = processor.serializeNBT();

        var restored = new TestProcessingMachine(new TestRecipeManager().add(RECIPE_TYPE, target));
        restored.deserializeNBT(saved);
        restored.onWorkContinue(recipe, machine);
        var restoredTag = restored.serializeNBT();

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
            processor.newRecipe(machine, marker.loc()).map(ProcessingRecipe::loc));
        assertEquals(Optional.of(other.loc()), processor.newRecipe(machine, other.loc()).map(ProcessingRecipe::loc));
    }

    @Test
    void shouldLeaveInputFiltersUnchangedWhenTargetRecipeDoesNotResolve() {
        var input = new TestPort("ore", 16, 0);
        var machine = new TestMachine(new TestContainer().port(INPUT_PORT, INPUT, input));
        var processor = new TestProcessingMachine();

        processor.setTargetRecipe(new ResourceLocation("tinactory", "missing"), machine);

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

        assertEquals(Optional.of(matching.loc()), resolved.map(ProcessingRecipe::loc));
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

        assertEquals(1, processor.calculateParallelForTest(recipe, machine, 1));
        assertEquals(3, processor.calculateParallelForTest(recipe, machine, 4));
    }

    @Test
    void shouldCalculateLowVoltageAndOverclockFactors() {
        var recipe = recipe("factors", 8);
        var lowVoltageMachine = new TestMachine(new TestContainer()).electricVoltage(8);
        var overclockedMachine = new TestMachine(new TestContainer()).electricVoltage(128);
        var processor = new TestProcessingMachine();

        processor.calculateFactorsForTest(recipe, lowVoltageMachine, 1);
        assertEquals(ProcessingMachine.PROGRESS_PER_TICK, processor.onWorkProgress(recipe, 1d));
        assertEquals(8d, processor.powerCons(recipe));

        processor.calculateFactorsForTest(recipe, overclockedMachine, 1);
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
        var saved = original.serializeNBT();

        var restored = new TestProcessingMachine(new TestRecipeManager());
        restored.deserializeNBT(saved);
        restored.onWorkContinue(recipe, machine);

        assertFalse(restored.serializeNBT().contains("filterRecipe"));
    }

    private static TestRecipe recipe(String path, long voltage) {
        return new TestRecipe.Builder(RECIPE_TYPE, new ResourceLocation("tinactory", path))
            .input(INPUT_PORT, new TestIngredient("ore", 1))
            .output(OUTPUT_PORT, new TestResult("dust", 1))
            .workTicks(20)
            .voltage(voltage)
            .power(8)
            .buildObject();
    }

    private static MarkerRecipe marker(String path, long voltage) {
        return new MarkerRecipe.Builder(MARKER_TYPE, new ResourceLocation("tinactory", path))
            .baseType(RECIPE_TYPE.loc())
            .prefix("ore")
            .workTicks(20)
            .voltage(voltage)
            .power(8)
            .buildObject();
    }

    private static final class TestProcessingMachine extends ProcessingMachine<TestRecipe> {
        private TestProcessingMachine() {
            this(new TestRecipeManager());
        }

        private TestProcessingMachine(TestRecipeManager recipeManager) {
            super(RECIPE_TYPE, recipeManager, MARKER_TYPE);
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

    private static final class TestRecipe extends ProcessingRecipe {
        private TestRecipe(Builder builder) {
            super(builder);
        }

        private static final class Builder extends BuilderBase<TestRecipe, Builder> {
            private Builder(IRecipeType<Builder> type, ResourceLocation loc) {
                super(type, loc);
            }

            @Override
            protected TestRecipe createObject() {
                return new TestRecipe(this);
            }
        }
    }
}
