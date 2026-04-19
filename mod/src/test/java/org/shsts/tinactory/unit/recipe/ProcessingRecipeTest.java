package org.shsts.tinactory.unit.recipe;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestProcessingObject;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.ArrayList;
import java.util.Collection;
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
            .input(0, new TestProcessingObject("ore", 2))
            .input(1, new TestProcessingObject("coolant", 1))
            .output(2, new TestProcessingObject("ingot", 1))
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
            .input(0, new TestProcessingObject("ore", 1))
            .output(2, new TestProcessingObject("ingot", 1))
            .output(2, new TestProcessingObject("ingot", 1))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 1, 0, 1));

        assertTrue(recipe.matchOutputsForTest(new TestMachine(container), container, 1, new Random(1L)));
    }

    @Test
    void shouldEmitConsumeAndInsertCallbacksWithProcessingInfo() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingObject("ore", 2))
            .input(1, new TestProcessingObject("coolant", 1))
            .output(2, new TestProcessingObject("ingot", 3))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(1, PortDirection.INPUT, new TestPort("coolant", 10, 2))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 10, 0));
        var consumed = new ArrayList<ProcessingInfo>();
        var inserted = new ArrayList<TestProcessingObject>();

        recipe.consumeInputs(container, 1, consumed::add);
        recipe.insertOutputs(container, 1, new Random(2L), result -> inserted.add((TestProcessingObject) result));

        assertIterableEquals(List.of(
            new ProcessingInfo(0, new TestProcessingObject("ore", 2)),
            new ProcessingInfo(1, new TestProcessingObject("coolant", 1))
        ), consumed);
        assertEquals(List.of(new TestProcessingObject("ingot", 3)), inserted);
        assertEquals(2, container.getTestPort(0).stored());
        assertEquals(1, container.getTestPort(1).stored());
        assertEquals(3, container.getTestPort(2).stored());
    }

    @Test
    void shouldConsumeAndInsertGenericExactStacksThroughRecipePolicy() {
        var recipe = recipeBuilder()
            .input(0, new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("ore", 2), TestStack.ADAPTER))
            .output(1, new StackResult<>("test_stack_result", PortType.ITEM, 1d,
                TestStack.item("ingot", 3), TestStack.ADAPTER))
            .buildObject();
        var container = new TestStackContainer()
            .port(0, PortDirection.INPUT, new TestStackPort(PortType.ITEM, "ore", "", 4, 16))
            .port(1, PortDirection.OUTPUT, new TestStackPort(PortType.ITEM, "ingot", "", 0, 16));
        var machine = new TestMachine(container);
        var consumed = new ArrayList<ProcessingInfo>();
        var inserted = new ArrayList<Object>();

        assertTrue(recipe.matchesForTest(machine, 1, new Random(1L)));

        recipe.consumeInputs(container, 1, consumed::add);
        recipe.insertOutputs(container, 1, new Random(2L), inserted::add);

        assertIterableEquals(List.of(
            new ProcessingInfo(0, new StackIngredient<>("test_stack_ingredient", PortType.ITEM,
                TestStack.item("ore", 2), TestStack.ADAPTER))
        ), consumed);
        assertEquals(List.of(new StackResult<>("test_stack_result", PortType.ITEM, 1d,
            TestStack.item("ingot", 3), TestStack.ADAPTER)), inserted);
        assertEquals(2, container.port(0).storedAmount());
        assertEquals(3, container.port(1).storedAmount());
    }

    @Test
    void shouldRequireEnoughMachineVoltageToCraft() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingObject("ore", 1))
            .output(1, new TestProcessingObject("ingot", 1))
            .voltage(120)
            .buildObject();

        assertFalse(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(32)));
        assertTrue(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(120)));
    }

    @Test
    void shouldBypassOutputChecksWhenAutoVoidIsEnabled() {
        var recipe = recipeBuilder()
            .input(0, new TestProcessingObject("ore", 1))
            .output(1, new TestProcessingObject("ingot", 1))
            .buildObject();
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 1))
            .port(1, PortDirection.OUTPUT, new TestPort("ingot", 1, 1));
        var blockingMachine = new TestMachine(container);
        var autoVoidMachine = new TestMachine(container).autoVoid(true);

        assertFalse(recipe.matchesForTest(blockingMachine, 1, new Random(3L)));
        assertTrue(recipe.matchesForTest(autoVoidMachine, 1, new Random(3L)));
    }

    @Test
    void shouldAllowAssemblyRecipeWithoutRequiredTech() {
        var recipe = assemblyBuilder()
            .buildObject();

        assertTrue(recipe.canCraft(new TestMachine(new TestContainer())));
    }

    @Test
    void shouldRejectAssemblyRecipeWhenRequiredTechIsMissing() {
        var tech = new ResourceLocation("tinactory", "assembler");
        var recipe = assemblyBuilder()
            .requireTech(tech)
            .buildObject();
        var machine = new TestMachine(new TestContainer());
        machine.team();

        assertFalse(recipe.canCraft(machine));
    }

    @Test
    void shouldAcceptAssemblyRecipeWhenRequiredTechIsFinished() {
        var tech = new ResourceLocation("tinactory", "assembler");
        var recipe = assemblyBuilder()
            .requireTech(tech)
            .buildObject();
        var machine = new TestMachine(new TestContainer());
        machine.team().finished(tech);

        assertTrue(recipe.canCraft(machine));
    }

    @Test
    void shouldRequireActiveMatchingResearchTarget() {
        var target = new ResourceLocation("tinactory", "research_target");
        var other = new ResourceLocation("tinactory", "other_research");
        var recipe = researchBuilder(target)
            .buildObject();
        var missingTargetMachine = new TestMachine(new TestContainer());
        missingTargetMachine.team().available(target);
        var otherTargetMachine = new TestMachine(new TestContainer());
        otherTargetMachine.team().target(other, 100L);
        var targetMachine = new TestMachine(new TestContainer());
        targetMachine.team().target(target, 100L);

        assertFalse(recipe.canCraft(missingTargetMachine));
        assertFalse(recipe.canCraft(otherTargetMachine));
        assertTrue(recipe.canCraft(targetMachine));
    }

    @Test
    void shouldCheckResearchCapacityForParallelProgress() {
        var target = new ResourceLocation("tinactory", "research_target");
        var recipe = researchBuilder(target)
            .progress(4L)
            .buildObject();
        var machine = new TestMachine(new TestContainer());
        machine.team()
            .target(target, 10L)
            .progress(target, 3L);

        assertTrue(recipe.matches(machine, 1));
        assertFalse(recipe.matches(machine, 2));
    }

    @Test
    void shouldAdvanceServerSideTechProgressWhenResearchOutputsAreInserted() {
        var target = new ResourceLocation("tinactory", "research_target");
        var recipe = researchBuilder(target)
            .progress(4L)
            .buildObject();
        var machine = new TestMachine(new TestContainer());
        var team = machine.team()
            .target(target, 100L)
            .progress(target, 2L);

        recipe.insertOutputs(machine, 3, new Random(4L), result -> {});

        assertEquals(14L, team.getTechProgress(target));
    }

    @Test
    void shouldRejectNonMultiblockMachineWhenMarkerRequiresMultiblock() {
        var recipe = markerBuilder()
            .requireMultiblock(true)
            .buildObject();

        assertFalse(recipe.canCraft(new TestMachine(new TestContainer())));
    }

    @Test
    void shouldAcceptExplicitMultiblockMachineWhenMarkerRequiresMultiblock() {
        var recipe = markerBuilder()
            .requireMultiblock(true)
            .buildObject();

        assertTrue(recipe.canCraft(new TestMachine(new TestContainer()).multiblock(true)));
    }

    @Test
    void shouldMatchMarkerBaseTypeAndPrefixByLocation() {
        var baseType = new ResourceLocation("tinactory", "base_type");
        var otherType = new ResourceLocation("tinactory", "other_type");
        var recipe = markerBuilder()
            .baseType(baseType)
            .prefix("ore")
            .buildObject();

        assertTrue(recipe.matchesType(baseType));
        assertFalse(recipe.matchesType(otherType));
        assertTrue(recipe.matches(() -> new ResourceLocation("tinactory", "ore")));
        assertTrue(recipe.matches(() -> new ResourceLocation("tinactory", "ore/copper")));
        assertFalse(recipe.matches(() -> new ResourceLocation("tinactory", "dust/copper")));
    }

    private static TestRecipe.Builder recipeBuilder() {
        return new TestRecipe.Builder(new ResourceLocation("tinactory", "test_recipe"));
    }

    private static AssemblyRecipe.Builder assemblyBuilder() {
        return new AssemblyRecipe.Builder(null, new ResourceLocation("tinactory", "test_assembly"))
            .output(0, new TestProcessingObject("assembly", 1))
            .workTicks(20L)
            .power(8L);
    }

    private static ResearchRecipe.Builder researchBuilder(ResourceLocation target) {
        return new ResearchRecipe.Builder(null, new ResourceLocation("tinactory", "test_research"))
            .target(target)
            .workTicks(20L)
            .power(8L);
    }

    private static MarkerRecipe.Builder markerBuilder() {
        return new MarkerRecipe.Builder(null, new ResourceLocation("tinactory", "test_marker"))
            .baseType(new ResourceLocation("tinactory", "test_base"))
            .workTicks(20L)
            .power(8L);
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

    private static final class TestStackContainer implements IContainer {
        private final java.util.Map<Integer, TestStackPort> ports = new java.util.HashMap<>();
        private final java.util.Map<Integer, PortDirection> directions = new java.util.HashMap<>();

        private TestStackContainer port(int index, PortDirection direction, TestStackPort port) {
            ports.put(index, port);
            directions.put(index, direction);
            return this;
        }

        private TestStackPort port(int index) {
            return ports.get(index);
        }

        @Override
        public int portSize() {
            return ports.size();
        }

        @Override
        public boolean hasPort(int port) {
            return ports.containsKey(port);
        }

        @Override
        public PortDirection portDirection(int port) {
            return directions.getOrDefault(port, PortDirection.NONE);
        }

        @Override
        public IPort<?> getPort(int port, org.shsts.tinactory.api.logistics.ContainerAccess access) {
            return ports.containsKey(port) ? ports.get(port) : IPort.empty();
        }
    }

    private static final class TestStackPort implements IPort<TestStack> {
        private final PortType type;
        private final String id;
        private final String nbt;
        private final int capacity;
        private int storedAmount;

        private TestStackPort(PortType type, String id, String nbt, int storedAmount, int capacity) {
            this.type = type;
            this.id = id;
            this.nbt = nbt;
            this.storedAmount = storedAmount;
            this.capacity = capacity;
        }

        @Override
        public PortType type() {
            return type;
        }

        @Override
        public boolean acceptInput(TestStack stack) {
            return type == stack.type() &&
                id.equals(stack.id()) &&
                nbt.equals(stack.nbt()) &&
                storedAmount < capacity;
        }

        @Override
        public TestStack insert(TestStack stack, boolean simulate) {
            if (!acceptInput(stack)) {
                return stack;
            }
            var inserted = Math.min(stack.amount(), capacity - storedAmount);
            if (!simulate) {
                storedAmount += inserted;
            }
            return TestStack.ADAPTER.withAmount(stack, stack.amount() - inserted);
        }

        @Override
        public TestStack extract(TestStack stack, boolean simulate) {
            if (type != stack.type() || !id.equals(stack.id()) || !nbt.equals(stack.nbt()) || storedAmount <= 0) {
                return TestStack.ADAPTER.empty();
            }
            var moved = Math.min(stack.amount(), storedAmount);
            if (!simulate) {
                storedAmount -= moved;
            }
            return TestStack.ADAPTER.withAmount(stack, moved);
        }

        @Override
        public TestStack extract(int limit, boolean simulate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorageAmount(TestStack stack) {
            return type == stack.type() && id.equals(stack.id()) && nbt.equals(stack.nbt()) ? storedAmount : 0;
        }

        @Override
        public Collection<TestStack> getAllStorages() {
            return storedAmount > 0 ? List.of(new TestStack(type, id, nbt, storedAmount)) : List.of();
        }

        @Override
        public boolean acceptOutput() {
            return storedAmount > 0;
        }

        private int storedAmount() {
            return storedAmount;
        }
    }
}
