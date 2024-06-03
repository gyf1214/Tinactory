package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.RecipeProcessor;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.ComposeDrawable;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory extends RecipeCategory<ProcessingRecipe, ProcessingMenu> {
    private static final int EXTRA_HEIGHT = (FONT_HEIGHT + SPACING_VERTICAL) * 3 + SLOT_SIZE / 2;
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.00");

    public ProcessingCategory(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType,
                              Layout layout, Block icon) {
        super(recipeType, layout, Ingredient.of(AllTags.processingMachine(recipeType)),
                new ItemStack(icon), ProcessingMenu.class);
    }

    @Override
    protected ComposeDrawable.Builder buildBackground(ComposeDrawable.Builder builder,
                                                      IGuiHelper helper, int xOffset) {
        return super.buildBackground(builder, helper, xOffset)
                .add(helper.createBlankDrawable(WIDTH, EXTRA_HEIGHT), 0, layout.rect.endY());
    }

    protected int drawTextLine(PoseStack stack, Component text, int y) {
        RenderUtil.renderText(stack, text, 0, y);
        return y - FONT_HEIGHT - SPACING_VERTICAL;
    }

    @Override
    protected void drawExtra(ProcessingRecipe recipe, IDrawHelper helper, IRecipeSlotsView recipeSlotsView,
                             PoseStack stack, double mouseX, double mouseY) {

        helper.drawProgressBar(stack, (int) recipe.workTicks);
        var y = helper.getBackground().getHeight() - FONT_HEIGHT;
        var total = recipe.power * recipe.workTicks;
        var duration = NUMBER_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).id.toUpperCase();
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.duration", duration), y);
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.usage", recipe.power, voltage), y);
        drawTextLine(stack, I18n.tr("tinactory.jei.processing.total", total), y);
    }

    private void addIngredient(IIngredientBuilder builder, Layout.SlotInfo slot, IProcessingObject ingredient) {
        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            builder.item(slot, item.stack());
        } else if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase item) {
            var items = Arrays.stream(item.ingredient.getItems())
                    .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, item.amount))
                    .toList();
            builder.items(slot, items);
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            builder.fluid(slot, fluid.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult item) {
            builder.item(slot, item.stack);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            builder.fluid(slot, fluid.stack);
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                    .formatted(ingredient.getClass()));
        }
    }

    @Override
    protected void addRecipe(ProcessingRecipe recipe, IIngredientBuilder builder) {
        var inputs = layout.getProcessingInputs(recipe);
        var outputs = layout.getProcessingOutputs(recipe);

        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        for (var output : outputs) {
            addIngredient(builder, output.slot(), output.val());
        }
    }

    @Override
    protected boolean canTransfer(ProcessingMenu menu, ProcessingRecipe recipe) {
        return AllCapabilities.PROCESSOR.tryGet(menu.blockEntity)
                .map(p -> p instanceof RecipeProcessor<?> processor &&
                        processor.recipeType == recipe.getType())
                .orElse(false);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // TODO: auto transfer of processing recipe is too buggy, disable now
    }
}
