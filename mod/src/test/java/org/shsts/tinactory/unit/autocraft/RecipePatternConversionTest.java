package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.integration.autocraft.ProcessingRecipePatternSource;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNull;

class RecipePatternConversionTest {
    @Test
    void convertRecipeShouldRejectUnsupportedIngredientOrResult() {
        var source = new ProcessingRecipePatternSource(new ResourceLocation("tinactory", "mixer"));

        var converted = source.convertRecipe(
            "tinactory:test",
            List.of(new UnsupportedIngredient()),
            List.of(new UnsupportedResult()));

        assertNull(converted);
    }

    @Test
    void convertRecipeShouldRejectEmptyOutputs() {
        var source = new ProcessingRecipePatternSource(new ResourceLocation("tinactory", "mixer"));

        var converted = source.convertRecipe("tinactory:test", List.of(), List.of());

        assertNull(converted);
    }

    private static final class UnsupportedIngredient implements IProcessingIngredient {
        @Override
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            return Optional.empty();
        }

        @Override
        public String codecName() {
            return "unsupported";
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }
    }

    private static final class UnsupportedResult implements IProcessingResult {
        @Override
        public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random, boolean simulate) {
            return Optional.empty();
        }

        @Override
        public String codecName() {
            return "unsupported";
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }
    }
}
