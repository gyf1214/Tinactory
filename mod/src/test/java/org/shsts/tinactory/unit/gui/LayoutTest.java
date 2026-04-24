package org.shsts.tinactory.unit.gui;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestRecipeType;
import org.shsts.tinactory.unit.fixture.TestResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutTest {
    @Test
    void shouldComputeBoundsOffsetPortsAndRecipeSlotMappings() {
        var imageTexture = new Texture(new ResourceLocation("tinactory", "gui/layout_image"), 20, 10);
        var progressTexture = new Texture(new ResourceLocation("tinactory", "gui/layout_progress"), 8, 80);
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

        var recipe = new ProcessingRecipe.Builder(null, new ResourceLocation("tinactory", "layout_recipe"))
            .input(0, new TestIngredient("ore", 1))
            .input(0, new TestIngredient("dust", 2))
            .input(1, new TestIngredient("catalyst", 1))
            .output(2, new TestResult("plate", 1))
            .output(2, new TestResult("gear", 1))
            .workTicks(20)
            .power(8)
            .buildObject();

        assertEquals(List.of(
            new Layout.SlotWith<>(new Layout.SlotInfo(0, 10, 20, 0, SlotType.ITEM_INPUT), new TestIngredient("ore", 1)),
            new Layout.SlotWith<>(new Layout.SlotInfo(1, 28, 20, 0, SlotType.ITEM_INPUT), new TestIngredient("dust", 2)),
            new Layout.SlotWith<>(new Layout.SlotInfo(2, 10, 50, 1, SlotType.ITEM_INPUT), new TestIngredient("catalyst", 1))),
            layout.getProcessingInputs(recipe));
        assertEquals(List.of(
            new Layout.SlotWith<>(new Layout.SlotInfo(3, 90, 20, 2, SlotType.ITEM_OUTPUT), new TestResult("plate", 1)),
            new Layout.SlotWith<>(new Layout.SlotInfo(4, 108, 20, 2, SlotType.ITEM_OUTPUT), new TestResult("gear", 1))),
            layout.getProcessingOutputs(recipe));

        var marker = new MarkerRecipe.Builder(
            new TestRecipeType<>("layout_marker_type", MarkerRecipe.class, MarkerRecipe.Builder::new),
            new ResourceLocation("tinactory", "layout_marker"))
            .baseType(new ResourceLocation("tinactory", "layout_base"))
            .output(2, new TestIngredient("marker_plate", 1))
            .output(2, new TestIngredient("marker_gear", 3))
            .workTicks(20)
            .power(8)
            .buildObject();

        assertEquals(List.of(
            new Layout.SlotWith<>(new Layout.SlotInfo(3, 90, 20, 2, SlotType.ITEM_OUTPUT),
                new TestIngredient("marker_plate", 1)),
            new Layout.SlotWith<>(new Layout.SlotInfo(4, 108, 20, 2, SlotType.ITEM_OUTPUT),
                new TestIngredient("marker_gear", 3))),
            layout.getProcessingOutputs(marker));
    }
}
