package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.ComposeDrawable;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.shsts.tinactory.content.AllTags.machineTag;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.DOUBLE_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<R extends ProcessingRecipe> extends RecipeCategory<R> {
    private static final int EXTRA_HEIGHT = FONT_HEIGHT * 3 + SPACING * 2 + SLOT_SIZE / 2;

    public ProcessingCategory(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, Ingredient.of(machineTag(recipeType)), new ItemStack(icon));
    }

    public static TranslatableComponent tr(String key, Object... args) {
        return I18n.tr("tinactory.jei.processing." + key, args);
    }

    protected int extraHeight() {
        return EXTRA_HEIGHT;
    }

    @Override
    protected void buildBackground(ComposeDrawable.Builder builder,
        IGuiHelper helper, int xOffset) {
        super.buildBackground(builder, helper, xOffset);
        builder.add(helper.createBlankDrawable(WIDTH, extraHeight()),
            0, layout.rect.endY());
    }

    protected int drawTextLine(PoseStack stack, Component text, int y) {
        RenderUtil.renderText(stack, text, 0, y);
        return y + FONT_HEIGHT + SPACING;
    }

    protected int drawRequiredTechText(PoseStack stack, boolean empty, int y) {
        if (!empty) {
            y += (BUTTON_SIZE - FONT_HEIGHT) / 2 + 1;
            drawTextLine(stack, tr("requiredTech"), y);
            y += (BUTTON_SIZE + FONT_HEIGHT) / 2 + SPACING;
        } else {
            y += BUTTON_SIZE + SPACING;
        }
        return y;
    }

    /**
     * return endY
     */
    protected int drawExtraText(R recipe, int y, PoseStack stack) {
        return y;
    }

    @Override
    protected void drawExtra(R recipe, ICategoryDrawHelper helper,
        IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        helper.drawProgressBar(stack, (int) recipe.workTicks);
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        var total = recipe.power * recipe.workTicks;
        var duration = DOUBLE_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).displayName();

        y = drawExtraText(recipe, y, stack);

        y = drawTextLine(stack, tr("total", total), y);
        y = drawTextLine(stack, tr("power", recipe.power, voltage), y);
        drawTextLine(stack, tr("duration", duration), y);
    }

    protected void addIngredient(IIngredientBuilder builder, Layout.SlotInfo slot, IProcessingObject ingredient) {
        if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
            builder.itemInput(slot, item.stack());
        } else if (ingredient instanceof ProcessingIngredients.ItemsIngredientBase item) {
            if (item.amount <= 0) {
                builder.itemNotConsumedInput(slot, List.of(item.ingredient.getItems()));
            } else {
                var items = Arrays.stream(item.ingredient.getItems())
                    .map(stack -> ItemHandlerHelper.copyStackWithSize(stack, item.amount))
                    .toList();
                builder.itemInput(slot, items);
            }
        } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
            builder.fluidInput(slot, fluid.fluid());
        } else if (ingredient instanceof ProcessingResults.ItemResult item) {
            builder.itemOutput(slot, item.stack, item.rate);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            builder.fluidOutput(slot, fluid.stack, fluid.rate);
        } else {
            throw new IllegalArgumentException("Unknown processing ingredient type %s"
                .formatted(ingredient.getClass()));
        }
    }

    @Override
    protected void setRecipe(R recipe, IIngredientBuilder builder) {
        var inputs = layout.getProcessingInputs(recipe);
        var outputs = layout.getProcessingOutputs(recipe);

        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        for (var output : outputs) {
            addIngredient(builder, output.slot(), output.val());
        }
    }

    protected void addTechIngredient(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
        int x, int y, ResourceLocation loc) {
        builder.addSlot(role, x + 1 + xOffset, y + 1)
            .addIngredient(TechIngredientType.INSTANCE, new TechWrapper(loc))
            .setCustomRenderer(TechIngredientType.INSTANCE, TechIngredientRenderer.INSTANCE);
    }

    protected void addRequiredTech(IRecipeLayoutBuilder builder, Collection<ResourceLocation> techs) {
        var x = ClientUtil.getFont().width(tr("requiredTech")) + SPACING - layout.getXOffset();
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        for (var tech : techs) {
            addTechIngredient(builder, RecipeIngredientRole.OUTPUT, x, y, tech);
            x += BUTTON_SIZE;
        }
    }
}
