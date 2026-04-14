package org.shsts.tinactory.unit.core.machine;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.machine.ProcessingInfo;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestRecipeManager;
import org.shsts.tinactory.unit.fixture.TestRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

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
            .port(INPUT_PORT, INPUT, new org.shsts.tinactory.unit.fixture.TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new org.shsts.tinactory.unit.fixture.TestPort("dust", 16, 0)));
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
            .port(INPUT_PORT, INPUT, new org.shsts.tinactory.unit.fixture.TestPort("ore", 16, 4))
            .port(OUTPUT_PORT, OUTPUT, new org.shsts.tinactory.unit.fixture.TestPort("dust", 16, 0)));
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

        private void addOutputInfoForTest(TestRecipe recipe, int parallel,
            java.util.function.Consumer<ProcessingInfo> info) {
            addOutputInfo(recipe, parallel, info);
        }
    }

    private static final class TestRecipe extends ProcessingRecipe {
        private TestRecipe(Builder builder) {
            super(builder);
        }

        private static final class Builder extends BuilderBase<TestRecipe, Builder> {
            private Builder(org.shsts.tinycorelib.api.registrate.entry.IRecipeType<Builder> type,
                ResourceLocation loc) {
                super(type, loc);
            }

            @Override
            protected TestRecipe createObject() {
                return new TestRecipe(this);
            }
        }
    }

    private record TestIngredient(String name, int amount)
        implements org.shsts.tinactory.api.recipe.IProcessingIngredient {
        @Override
        public String codecName() {
            return "test_ingredient";
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Predicate<?> filter() {
            return (Predicate<TestResult>) other -> Objects.equals(name, other.name);
        }

        @Override
        public Optional<org.shsts.tinactory.api.recipe.IProcessingIngredient> consumePort(
            IPort<?> port, int parallel, boolean simulate) {
            if (port instanceof org.shsts.tinactory.unit.fixture.TestPort testPort) {
                return testPort.consume(name, amount * parallel, simulate).map($ -> this);
            }
            throw new UnsupportedOperationException();
        }
    }

    private record TestResult(String name, int amount) implements IProcessingResult {
        @Override
        public String codecName() {
            return "test_result";
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Predicate<?> filter() {
            return (Predicate<TestResult>) other -> Objects.equals(name, other.name);
        }

        @Override
        public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random, boolean simulate) {
            if (port instanceof org.shsts.tinactory.unit.fixture.TestPort testPort) {
                return testPort.insert(name, amount * parallel, simulate).map($ -> this);
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public IProcessingResult scaledPreview(int parallel) {
            return new TestResult(name, amount * parallel);
        }
    }
}
