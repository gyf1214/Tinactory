package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.DrawableHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<T extends ProcessingRecipe> extends RecipeCategory<T> {
    private static final int EXTRA_HEIGHT = (Menu.FONT_HEIGHT + Menu.SPACING_VERTICAL) * 5;
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.00");

    private static IDrawable createBackground(IJeiHelpers helpers, Layout layout) {
        var guiHelper = helpers.getGuiHelper();
        return DrawableHelper.createBackground(guiHelper, layout, WIDTH)
                .add(guiHelper.createBlankDrawable(WIDTH, EXTRA_HEIGHT), 0, layout.rect.endY())
                .build();
    }

    public ProcessingCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemLike icon) {
        super(type, helpers, createBackground(helpers, layout), layout, new ItemStack(icon));
    }

    private <I> void addIngredient(IRecipeLayoutBuilder builder, Layout.SlotInfo slot, I ingredient) {
        var role = switch (slot.type().direction) {
            case NONE -> RecipeIngredientRole.RENDER_ONLY;
            case INPUT -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
        };
        var slotBuilder = builder.addSlot(role, slot.x() + 1 + xOffset, slot.y() + 1);
        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            slotBuilder.addItemStack(item.stack());
        } else if (ingredient instanceof ProcessingIngredients.TagIngredient tag) {
            var stacks = Arrays.stream(tag.ingredient.getItems())
                    .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, tag.amount))
                    .toList();
            slotBuilder.addItemStacks(stacks);
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            slotBuilder.addIngredient(ForgeTypes.FLUID_STACK, fluid.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult item) {
            slotBuilder.addItemStack(item.stack);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            slotBuilder.addIngredient(ForgeTypes.FLUID_STACK, fluid.stack);
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                    .formatted(ingredient.getClass()));
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        var inputs = layout.getProcessingInputs(recipe);
        var outputs = layout.getProcessingOutputs(recipe);

        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        for (var output : outputs) {
            addIngredient(builder, output.slot(), output.val());
        }
    }

    protected int drawTextLine(PoseStack stack, Component text, int y) {
        RenderUtil.renderText(stack, text, 0, y);
        return y - Menu.FONT_HEIGHT - Menu.SPACING_VERTICAL;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        drawProgressBar(stack, (int) recipe.workTicks);
        var y = background.getHeight() - Menu.FONT_HEIGHT;
        var total = recipe.power * recipe.workTicks;
        var duration = NUMBER_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).id.toUpperCase();
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.duration", duration), y);
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.usage", recipe.power, voltage), y);
        drawTextLine(stack, I18n.tr("tinactory.jei.processing.total", total), y);
    }
}
