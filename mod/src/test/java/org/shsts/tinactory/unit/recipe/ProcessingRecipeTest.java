package org.shsts.tinactory.unit.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.TextureRenderDescriptor;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestPort;
import org.shsts.tinactory.unit.fixture.TestProcessingObject;
import org.shsts.tinactory.unit.fixture.TestRecipe;
import org.shsts.tinactory.unit.fixture.TestResult;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.input;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.inputStack;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.output;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.outputStack;

class ProcessingRecipeTest {
    @Test
    void shouldMatchInputsWhenEveryIngredientCanBeConsumed() {
        var recipe = testRecipe(List.of(input(0, "ore", 2), input(1, "coolant", 1)),
            List.of(output(2, "ingot", 1)));

        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(1, PortDirection.INPUT, new TestPort("coolant", 10, 1))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 10, 0));

        assertTrue(recipe.matchInputsForTest(new TestMachine(container), container, 1));
        assertFalse(recipe.matchInputsForTest(new TestMachine(container), container, 3));
    }

    @Test
    void shouldRespectOutputPortLimitWhenCheckingOutputSpace() {
        var recipe = testRecipe(List.of(input(0, "ore", 1)),
            List.of(output(2, "ingot", 1), output(2, "ingot", 1)));

        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 1, 0, 1));

        assertTrue(recipe.matchOutputsForTest(new TestMachine(container), container, 1, RandomSource.create()));
    }

    @Test
    void shouldEmitConsumeAndInsertCallbacksWithProcessingInfo() {
        var recipe = testRecipe(List.of(input(0, "ore", 2), input(1, "coolant", 1)),
            List.of(output(2, "ingot", 3)));

        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 4))
            .port(1, PortDirection.INPUT, new TestPort("coolant", 10, 2))
            .port(2, PortDirection.OUTPUT, new TestPort("ingot", 10, 0));
        var consumed = new ArrayList<ProcessingInfo>();
        var inserted = new ArrayList<TestProcessingObject>();

        recipe.consumeInputs(container, 1, consumed::add);
        recipe.insertOutputs(container, 1, RandomSource.create(),
            result -> inserted.add((TestProcessingObject) result));

        assertIterableEquals(List.of(
            new ProcessingInfo(0, new TestIngredient("ore", 2)),
            new ProcessingInfo(1, new TestIngredient("coolant", 1))
        ), consumed);
        assertEquals(List.of(new TestResult("ingot", 3)), inserted);
        assertEquals(2, container.getTestPort(0).stored());
        assertEquals(1, container.getTestPort(1).stored());
        assertEquals(3, container.getTestPort(2).stored());
    }

    @Test
    void shouldDisplayPrimaryOutputBeforeInputFallback() {
        var outputDescriptor = new ItemIdRenderDescriptor(modLoc("display/output"));
        var outputTooltip = List.<Component>of(I18n.raw("output tooltip"));
        var inputDescriptor = new ItemIdRenderDescriptor(modLoc("display/input"));
        var inputTooltip = List.<Component>of(I18n.raw("input tooltip"));
        var recipe = testRecipe(
            List.of(new ProcessingRecipe.Input(4, new TestIngredient("ore", 1, inputDescriptor, inputTooltip))),
            List.of(new ProcessingRecipe.Output(3, new TestResult("ingot", 1, outputDescriptor, outputTooltip))));

        assertEquals(outputDescriptor, recipe.display());
        assertEquals(outputTooltip, recipe.tooltip(modLoc("test")).orElseThrow());
    }

    @Test
    void shouldFallbackToPrimaryInputWhenRecipeHasNoOutputs() {
        var earlierDescriptor = new ItemIdRenderDescriptor(modLoc("display/earlier"));
        var earlierTooltip = List.<Component>of(I18n.raw("earlier tooltip"));
        var laterDescriptor = new ItemIdRenderDescriptor(modLoc("display/later"));
        var laterTooltip = List.<Component>of(I18n.raw("later tooltip"));

        var recipe = testRecipe(
            List.of(new ProcessingRecipe.Input(4, new TestIngredient("ore", 1, laterDescriptor, laterTooltip)),
                new ProcessingRecipe.Input(1, new TestIngredient("dust", 1, earlierDescriptor, earlierTooltip))),
            List.of());

        assertEquals(earlierDescriptor, recipe.display());
        assertEquals(earlierTooltip, recipe.tooltip(modLoc("test_no_output")).orElseThrow());
    }

    @Test
    void shouldFallbackToEmptyDescriptorWhenRepresentativeObjectHasNoDisplay() {
        var recipe = testRecipe(List.of(), List.of());

        assertSame(EmptyRenderDescriptor.INSTANCE, recipe.display());
        assertTrue(recipe.tooltip(modLoc("test_empty")).isEmpty());
    }

    @Test
    void shouldUseFirstInputForDisplayInputRecipePresentation() {
        var firstDescriptor = new ItemIdRenderDescriptor(modLoc("display/first"));
        var firstTooltip = List.<Component>of(I18n.raw("first tooltip"));
        var secondDescriptor = new ItemIdRenderDescriptor(modLoc("display/second"));
        var secondTooltip = List.<Component>of(I18n.raw("second tooltip"));

        var recipe = new DisplayInputRecipe(
            List.of(new ProcessingRecipe.Input(7, new TestIngredient("ore", 1, firstDescriptor, firstTooltip)),
                new ProcessingRecipe.Input(1, new TestIngredient("dust", 1, secondDescriptor, secondTooltip))),
            List.of(output(0, "plate", 1)),
            20, 0, 8);

        assertEquals(firstDescriptor, recipe.display());
        assertEquals(firstTooltip, recipe.tooltip(modLoc("test_display_input")).orElseThrow());
    }

    @Test
    void shouldUseMarkerDisplayIngredientDescriptorAndRecipeTooltip() {
        var displayDescriptor = new ItemIdRenderDescriptor(modLoc("display/marker"));
        var displayTooltip = List.<Component>of(I18n.raw("ignored display tooltip"));
        var loc = modLoc("test_marker");

        var recipe = markerRecipe(input(0, "marker_output", 1),
            Optional.of(new TestIngredient("display", 1, displayDescriptor, displayTooltip)),
            Optional.empty());

        assertEquals(displayDescriptor, recipe.display());
        assertEquals(List.of(I18n.tr(ProcessingRecipe.getDescriptionId(loc))),
            recipe.tooltip(loc).orElseThrow());
    }

    @Test
    void shouldUseMarkerTextureDescriptorAndRecipeTooltip() {
        var textureLoc = modLoc("gui/marker");
        var loc = modLoc("test_marker_tex");

        var recipe = markerRecipe(input(0, "marker_output", 1),
            Optional.empty(),
            Optional.of(textureLoc));

        assertEquals(new TextureRenderDescriptor(new Texture(textureLoc, 16, 16)), recipe.display());
        assertEquals(List.of(I18n.tr(ProcessingRecipe.getDescriptionId(loc))),
            recipe.tooltip(loc).orElseThrow());
    }

    @Test
    void shouldFallbackMarkerDescriptorToRepresentativeObjectButKeepRecipeTooltip() {
        var inputDescriptor = new ItemIdRenderDescriptor(modLoc("display/input"));
        var inputTooltip = List.<Component>of(I18n.raw("ignored input tooltip"));
        var loc = modLoc("test_marker_no_display");

        var recipe = new MarkerRecipe(
            List.of(new ProcessingRecipe.Input(0, new TestIngredient("ore", 1, inputDescriptor, inputTooltip))),
            List.of(), 0, modLoc("test_base"), "",
            false, Optional.empty(), Optional.empty(), List.of());

        assertEquals(inputDescriptor, recipe.display());
        assertEquals(List.of(I18n.tr(ProcessingRecipe.getDescriptionId(loc))),
            recipe.tooltip(loc).orElseThrow());
    }

    @Test
    void shouldConsumeAndInsertGenericExactStacksThroughRecipePolicy() {
        var recipe = testRecipe(
            List.of(inputStack(0, "ore", 2)),
            List.of(outputStack(1, "ingot", 3)));
        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort(PortType.ITEM, "ore", "", 4, 16))
            .port(1, PortDirection.OUTPUT, new TestPort(PortType.ITEM, "ingot", "", 0, 16));
        var machine = new TestMachine(container);
        var consumed = new ArrayList<ProcessingInfo>();
        var inserted = new ArrayList<IProcessingResult>();

        assertTrue(recipe.matchesForTest(machine, 1, RandomSource.create()));

        recipe.consumeInputs(container, 1, consumed::add);
        recipe.insertOutputs(container, 1, RandomSource.create(), inserted::add);

        assertIterableEquals(List.of(new ProcessingInfo(0, new StackIngredient<>("test_stack_ingredient",
            PortType.ITEM, TestStack.item("ore", 2), TestStack.ADAPTER))), consumed);
        assertEquals(List.of(new StackResult<>("test_stack_result", PortType.ITEM, 1d,
            TestStack.item("ingot", 3), TestStack.ADAPTER)), inserted);
        assertEquals(2, container.getTestPort(0).stored());
        assertEquals(3, container.getTestPort(1).stored());
    }

    @Test
    void shouldRequireEnoughMachineVoltageToCraft() {
        var recipe = new TestRecipe(
            List.of(input(0, "ore", 1)),
            List.of(output(1, "ingot", 1)),
            20, 120, 8);

        assertFalse(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(32)));
        assertTrue(recipe.canCraft(new TestMachine(new TestContainer()).electricVoltage(120)));
    }

    @Test
    void shouldBypassOutputChecksWhenAutoVoidIsEnabled() {
        var recipe = testRecipe(
            List.of(input(0, "ore", 1)),
            List.of(output(1, "ingot", 1)));

        var container = new TestContainer()
            .port(0, PortDirection.INPUT, new TestPort("ore", 10, 1))
            .port(1, PortDirection.OUTPUT, new TestPort("ingot", 1, 1));
        var blockingMachine = new TestMachine(container);
        var autoVoidMachine = new TestMachine(container).autoVoid(true);

        assertFalse(recipe.matchesForTest(blockingMachine, 1, RandomSource.create()));
        assertTrue(recipe.matchesForTest(autoVoidMachine, 1, RandomSource.create()));
    }

    @Test
    void shouldAllowAssemblyRecipeWithoutRequiredTech() {
        var recipe = assemblyRecipe(List.of());

        assertTrue(recipe.canCraft(new TestMachine(new TestContainer())));
    }

    @Test
    void shouldRejectAssemblyRecipeWhenRequiredTechIsMissing() {
        var tech = modLoc("assembler");
        var recipe = assemblyRecipe(List.of(tech));
        var machine = new TestMachine(new TestContainer());
        machine.team();

        assertFalse(recipe.canCraft(machine));
    }

    @Test
    void shouldAcceptAssemblyRecipeWhenRequiredTechIsFinished() {
        var tech = modLoc("assembler");
        var recipe = assemblyRecipe(List.of(tech));
        var machine = new TestMachine(new TestContainer());
        machine.team().finished(tech);

        assertTrue(recipe.canCraft(machine));
    }

    @Test
    void shouldRequireActiveMatchingResearchTarget() {
        var target = modLoc("research_target");
        var other = modLoc("other_research");
        var recipe = researchRecipe(target, 1);
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
        var target = modLoc("research_target");
        var recipe = researchRecipe(target, 4);
        var machine = new TestMachine(new TestContainer());
        machine.team()
            .target(target, 10L)
            .progress(target, 3L);

        assertTrue(recipe.matches(machine, 1));
        assertFalse(recipe.matches(machine, 2));
    }

    @Test
    void shouldAdvanceServerSideTechProgressWhenResearchOutputsAreInserted() {
        var target = modLoc("research_target");
        var recipe = researchRecipe(target, 4);
        var machine = new TestMachine(new TestContainer());
        var team = machine.team()
            .target(target, 100L)
            .progress(target, 2L);

        recipe.insertOutputs(machine, 3, RandomSource.create(), result -> {});

        assertEquals(14L, team.getTechProgress(target));
    }

    @Test
    void shouldOnlyAcceptMultiblockMachineWhenMarkerRequiresMultiblock() {
        var recipe = new MarkerRecipe(List.of(), List.of(), 0, modLoc("test_base"), "",
            true, Optional.empty(), Optional.empty(), List.of());

        assertFalse(recipe.canCraft(new TestMachine(new TestContainer())));
        assertTrue(recipe.canCraft(new TestMachine(new TestContainer()).multiblock(true)));
    }

    @Test
    void shouldMatchMarkerBaseTypeAndPrefixByLocation() {
        var baseType = modLoc("base_type");
        var otherType = modLoc("other_type");
        var recipe = new MarkerRecipe(List.of(), List.of(), 0, baseType, "ore",
            true, Optional.empty(), Optional.empty(), List.of());

        assertTrue(recipe.matchesType(baseType));
        assertFalse(recipe.matchesType(otherType));
        assertTrue(recipe.matches(() -> modLoc("ore")));
        assertTrue(recipe.matches(() -> modLoc("ore/copper")));
        assertFalse(recipe.matches(() -> modLoc("dust/copper")));
    }

    private static TestRecipe testRecipe(List<ProcessingRecipe.Input> inputs,
        List<ProcessingRecipe.Output> outputs) {
        return new TestRecipe(inputs, outputs, 20, 0, 8);
    }

    private static MarkerRecipe markerRecipe(ProcessingRecipe.Input output,
        Optional<IProcessingIngredient> display, Optional<ResourceLocation> tex) {
        return new MarkerRecipe(List.of(), List.of(), 0, modLoc("test_base"), "",
            false, display, tex, List.of(output));
    }

    private static AssemblyRecipe assemblyRecipe(List<ResourceLocation> techs) {
        return new AssemblyRecipe(List.of(), List.of(output(0, "assembly", 1)),
            20, 0, 8, techs);
    }

    private static ResearchRecipe researchRecipe(ResourceLocation target, long progress) {
        return new ResearchRecipe(List.of(), 20, 0, 8, target, progress);
    }
}
