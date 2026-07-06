package org.shsts.tinactory.unit.gui;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.input;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.output;

class LayoutTest {
    @Test
    void shouldComputeBoundsOffsetPortsAndRecipeSlotMappings() {
        var imageTexture = new Texture(modLoc("gui/layout_image"), 20, 10);
        var progressTexture = new Texture(modLoc("gui/layout_progress"), 8, 80);
        var layout = Layout.builder()
            .port(SlotType.ITEM_INPUT)
            .slot(10, 20)
            .slot(28, 20)
            .port(SlotType.ITEM_INPUT)
            .slot(10, 50)
            .port(SlotType.ITEM_OUTPUT)
            .slot(90, 20)
            .slot(108, 20)
            .image(new Rect(120, 4, 20, 10), imageTexture)
            .progressBar(new Rect(140, 50, 8, 40), progressTexture)
            .buildLayout();

        assertEquals(new Rect(0, 0, 148, 90), layout.rect);
        assertEquals(7, layout.getXOffset());
        assertEquals(List.of(
            new Layout.PortInfo(2, SlotType.ITEM_INPUT),
            new Layout.PortInfo(1, SlotType.ITEM_INPUT),
            new Layout.PortInfo(2, SlotType.ITEM_OUTPUT)), layout.ports);
        assertEquals(List.of(
            new Layout.SlotInfo(0, 10, 20, 0, SlotType.ITEM_INPUT),
            new Layout.SlotInfo(1, 28, 20, 0, SlotType.ITEM_INPUT)), layout.portSlots.get(0));
        assertEquals(List.of(
            new Layout.SlotInfo(2, 10, 50, 1, SlotType.ITEM_INPUT)), layout.portSlots.get(1));
        assertEquals(List.of(
            new Layout.SlotInfo(3, 90, 20, 2, SlotType.ITEM_OUTPUT),
            new Layout.SlotInfo(4, 108, 20, 2, SlotType.ITEM_OUTPUT)), layout.portSlots.get(2));

        var recipe = new ProcessingRecipe(
            List.of(input(0, "ore", 1), input(0, "dust", 2), input(1, "catalyst", 1)),
            List.of(output(2, "plate", 1), output(2, "gear", 1)),
            20, 0, 8);

        assertEquals(
            List.of(inputSlot(0, 10, 20, 0, "ore", 1),
                inputSlot(1, 28, 20, 0, "dust", 2),
                inputSlot(2, 10, 50, 1, "catalyst", 1)),
            layout.getProcessingInputs(recipe));
        assertEquals(
            List.of(outputSlot(3, 90, 20, 2, "plate", 1), outputSlot(4, 108, 20, 2, "gear", 1)),
            layout.getProcessingOutputs(recipe));

        var marker = new MarkerRecipe(
            List.of(), List.of(), modLoc("layout_base"), "", false, Optional.empty(), Optional.empty(),
            List.of(input(2, "marker_plate", 1), input(2, "marker_gear", 3)));

        assertEquals(
            List.of(markerOutput(3, 90, 20, 2, "marker_plate", 1), markerOutput(4, 108, 20, 2, "marker_gear", 3)),
            layout.getProcessingOutputs(marker));
    }

    private static Layout.SlotWith<TestIngredient> inputSlot(int index, int x, int y, int port,
        String key, int amount) {
        return new Layout.SlotWith<>(new Layout.SlotInfo(index, x, y, port, SlotType.ITEM_INPUT),
            new TestIngredient(key, amount));
    }

    private static Layout.SlotWith<TestResult> outputSlot(int index, int x, int y, int port,
        String key, int amount) {
        return new Layout.SlotWith<>(new Layout.SlotInfo(index, x, y, port, SlotType.ITEM_OUTPUT),
            new TestResult(key, amount));
    }

    private static Layout.SlotWith<TestIngredient> markerOutput(int index, int x, int y, int port,
        String key, int amount) {
        return new Layout.SlotWith<>(new Layout.SlotInfo(index, x, y, port, SlotType.ITEM_OUTPUT),
            new TestIngredient(key, amount));
    }
}
