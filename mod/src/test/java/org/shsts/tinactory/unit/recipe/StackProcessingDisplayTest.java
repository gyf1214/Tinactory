package org.shsts.tinactory.unit.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.unit.fixture.TestStack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StackProcessingDisplayTest {
    @Test
    void shouldProjectStackIngredientDisplayAndTooltipThroughAdapter() {
        var stack = TestStack.item("ore", 2);
        var ingredient = new StackIngredient<>("test_stack_ingredient", PortType.ITEM, stack, TestStack.ADAPTER);

        assertEquals(new ItemIdRenderDescriptor(new ResourceLocation("tinactory", "ore")), ingredient.display());
        assertEquals(List.<Component>of(I18n.raw("item ore x2")), ingredient.tooltip().orElseThrow());
    }

    @Test
    void shouldProjectStackResultDisplayAndTooltipThroughAdapter() {
        var stack = TestStack.fluid("steam", 500);
        var result = new StackResult<>("test_stack_result", PortType.FLUID, 1d, stack, TestStack.ADAPTER);

        assertEquals(new ItemIdRenderDescriptor(new ResourceLocation("tinactory", "steam")), result.display());
        assertEquals(List.<Component>of(I18n.raw("fluid steam x500")), result.tooltip().orElseThrow());
    }
}
