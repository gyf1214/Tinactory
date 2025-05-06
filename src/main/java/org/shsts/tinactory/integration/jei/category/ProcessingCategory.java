package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.ComposeDrawable;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Arrays;
import java.util.List;

import static org.shsts.tinactory.content.AllTags.machineTag;
import static org.shsts.tinactory.content.gui.client.TechPanel.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.DOUBLE_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<R extends ProcessingRecipe> extends RecipeCategory<R> {
    private static final int EXTRA_HEIGHT = FONT_HEIGHT * 3 + SPACING * 2 + SLOT_SIZE / 2;

    public ProcessingCategory(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, Ingredient.of(machineTag(recipeType)), new ItemStack(icon));
    }

    @Override
    protected void buildBackground(ComposeDrawable.Builder builder,
        IGuiHelper helper, int xOffset) {
        var extraHeight = EXTRA_HEIGHT;
        if (AssemblyRecipe.class.isAssignableFrom(recipeType.recipeClass())) {
            extraHeight += BUTTON_SIZE + SPACING;
        } else if (ResearchRecipe.class.isAssignableFrom(recipeType.recipeClass())) {
            extraHeight += BUTTON_SIZE + FONT_HEIGHT + SPACING * 2;
        } else if (BlastFurnaceRecipe.class.isAssignableFrom(recipeType.recipeClass())) {
            extraHeight += FONT_HEIGHT + SPACING;
        }
        super.buildBackground(builder, helper, xOffset);
        builder.add(helper.createBlankDrawable(WIDTH, extraHeight), 0, layout.rect.endY());
    }

    private int drawTextLine(PoseStack stack, Component text, int y) {
        RenderUtil.renderText(stack, text, 0, y);
        return y + FONT_HEIGHT + SPACING;
    }

    private int drawRequiredText(PoseStack stack, boolean empty, int y) {
        if (!empty) {
            y += (BUTTON_SIZE - FONT_HEIGHT) / 2 + 1;
            drawTextLine(stack, I18n.tr("tinactory.jei.processing.requiredTech"), y);
            y += (BUTTON_SIZE + FONT_HEIGHT) / 2 + SPACING;
        } else {
            y += BUTTON_SIZE + SPACING;
        }
        return y;
    }

    @Override
    protected void drawExtra(ProcessingRecipe recipe, ICategoryDrawHelper helper, IRecipeSlotsView recipeSlotsView,
        PoseStack stack, double mouseX, double mouseY) {
        helper.drawProgressBar(stack, (int) recipe.workTicks);
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        var total = recipe.power * recipe.workTicks;
        var duration = DOUBLE_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).displayName();

        if (recipe instanceof AssemblyRecipe recipe1) {
            y = drawRequiredText(stack, recipe1.requiredTech.isEmpty(), y);
        } else if (recipe instanceof ResearchRecipe recipe1) {
            var tech = TechManager.client().techByKey(recipe1.target);
            if (tech.isPresent()) {
                y = drawRequiredText(stack, tech.get().getDepends().isEmpty(), y);
                var text = I18n.tr("tinactory.jei.processing.progress",
                    NUMBER_FORMAT.format(recipe1.progress),
                    NUMBER_FORMAT.format(tech.get().getMaxProgress()));
                y = drawTextLine(stack, text, y);
            }
        } else if (recipe instanceof BlastFurnaceRecipe recipe1) {
            var text = I18n.tr("tinactory.jei.processing.temperature",
                NUMBER_FORMAT.format(recipe1.temperature));
            y = drawTextLine(stack, text, y);
        }
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.total", total), y);
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.power", recipe.power, voltage), y);
        drawTextLine(stack, I18n.tr("tinactory.jei.processing.duration", duration), y);
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
    protected void setRecipe(ProcessingRecipe recipe, IIngredientBuilder builder) {
        var inputs = layout.getProcessingInputs(recipe);
        var outputs = layout.getProcessingOutputs(recipe);

        for (var input : inputs) {
            addIngredient(builder, input.slot(), input.val());
        }
        for (var output : outputs) {
            addIngredient(builder, output.slot(), output.val());
        }
    }

    private void addTechIngredient(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
        int x, int y, ResourceLocation loc) {
        builder.addSlot(role, x + 1 + xOffset, y + 1)
            .addIngredient(TechIngredientType.INSTANCE, new TechWrapper(loc))
            .setCustomRenderer(TechIngredientType.INSTANCE, TechIngredientRenderer.INSTANCE);
    }

    @Override
    protected void extraLayout(ProcessingRecipe recipe, IRecipeLayoutBuilder builder) {
        if (recipe instanceof ResearchRecipe recipe1) {
            var rect = layout.images.get(0).rect();
            addTechIngredient(builder, RecipeIngredientRole.OUTPUT, rect.x(), rect.y(), recipe1.target);
        }
        List<?> requiredTech = List.of();
        if (recipe instanceof AssemblyRecipe recipe1) {
            requiredTech = recipe1.requiredTech;
        } else if (recipe instanceof ResearchRecipe recipe1) {
            var tech = TechManager.client().techByKey(recipe1.target);
            if (tech.isPresent()) {
                requiredTech = tech.get().getDepends();
            }
        }
        var x = ClientUtil.getFont().width(I18n.tr("tinactory.jei.processing.requiredTech")) +
            SPACING - layout.getXOffset();
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        for (var tech : requiredTech) {
            ResourceLocation loc;
            if (tech instanceof ITechnology tech1) {
                loc = tech1.getLoc();
            } else if (tech instanceof ResourceLocation loc1) {
                loc = loc1;
            } else {
                throw new IllegalStateException();
            }
            addTechIngredient(builder, RecipeIngredientRole.OUTPUT, x, y, loc);
            x += BUTTON_SIZE;
        }
    }
}
