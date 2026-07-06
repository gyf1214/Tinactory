package org.shsts.tinactory.unit.recipe;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestIngredient;
import org.shsts.tinactory.unit.fixture.TestRecipe;
import org.shsts.tinactory.unit.fixture.TestRecipeType;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.INGREDIENT_CODEC;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.INPUT_CODEC;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.OUTPUT_CODEC;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.input;
import static org.shsts.tinactory.unit.fixture.TestProcessingHelper.output;

class ProcessingObjectCodecTest {
    @Test
    void shouldRoundTripProcessingRecipeWithInjectedCodecs() {
        var codec = ProcessingRecipe.codec(INPUT_CODEC, OUTPUT_CODEC, ProcessingRecipe::new);
        var recipe = new ProcessingRecipe(
            List.of(input(0, "ore", 2)),
            List.of(output(1, "ingot", 3)),
            40, 120, 16);

        var jo = CodecHelper.encodeJson(codec.encoder(), recipe);
        var roundTrip = CodecHelper.parseJson(codec.decoder(), jo);

        assertEquals(recipe.inputs, roundTrip.inputs);
        assertEquals(recipe.outputs, roundTrip.outputs);
        assertEquals(recipe.workTicks, roundTrip.workTicks);
        assertEquals(recipe.voltage, roundTrip.voltage);
        assertEquals(recipe.power, roundTrip.power);
    }

    @Test
    void shouldRoundTripResearchRecipeWithInjectedIngredientCodec() {
        var codec = ResearchRecipe.codec(INPUT_CODEC);
        var recipe = new ResearchRecipe(
            List.of(input(0, "scan", 1)),
            50, 32, 4, modLoc("research_target"), 7);

        var jo = CodecHelper.encodeJson(codec.encoder(), recipe);
        var roundTrip = CodecHelper.parseJson(codec.decoder(), jo);

        assertEquals(recipe.inputs, roundTrip.inputs);
        assertEquals(recipe.target, roundTrip.target);
        assertEquals(recipe.progress, roundTrip.progress);
    }

    @Test
    void shouldRoundTripMarkerRecipeWithBaseTypeId() {
        var codec = MarkerRecipe.codec(INGREDIENT_CODEC, INPUT_CODEC, OUTPUT_CODEC);

        var baseTypeId = modLoc("test_marker_base");
        var display = new TestIngredient("ore", 1);

        var recipe = new MarkerRecipe(
            List.of(), List.of(), baseTypeId, "test_marker_base/ores",
            true, Optional.of(display), Optional.empty(),
            List.of(input(1, "dust", 2)));

        var jo = CodecHelper.encodeJson(codec.encoder(), recipe);
        var roundTrip = CodecHelper.parseJson(codec.decoder(), jo);

        assertTrue(roundTrip.matchesType(new TestRecipeType<>("test_marker_base", TestRecipe.class)));
        assertTrue(roundTrip.matchesType(baseTypeId));
        assertEquals(display, roundTrip.displayIngredient().orElseThrow());
        assertEquals(recipe.markerOutputs, roundTrip.markerOutputs);
        assertTrue(roundTrip.matches(() -> modLoc("test_marker_base/ores/test")));
    }
}
