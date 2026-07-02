package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.compat.jei.ComposeDrawable;
import org.shsts.tinactory.compat.jei.ingredient.RecipeMarker;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredient;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredientRenderer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.shsts.tinactory.AllTags.machine;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Menu.TECH_SIZE;
import static org.shsts.tinactory.integration.util.ClientUtil.DOUBLE_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory<R extends ProcessingRecipe> extends RecipeCategory<R> {
    private static final int EXTRA_HEIGHT = FONT_HEIGHT * 3 + SPACING * 2 + SLOT_SIZE / 2;

    public ProcessingCategory(
        IRecipeType<R> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, Ingredient.of(machine(recipeType)), new ItemStack(icon));
    }

    public static Component tr(String key, Object... args) {
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

    protected int drawTextLine(GuiGraphics graphics, Component text, int y) {
        RenderUtil.renderText(graphics, text, 0, y);
        return y + FONT_HEIGHT + SPACING;
    }

    protected int drawRequiredTechText(GuiGraphics graphics, boolean empty, int y) {
        if (!empty) {
            y += (TECH_SIZE - FONT_HEIGHT) / 2 + 1;
            drawTextLine(graphics, tr("requiredTech"), y);
            y += (TECH_SIZE + FONT_HEIGHT) / 2 + SPACING;
        } else {
            y += TECH_SIZE + SPACING;
        }
        return y;
    }

    /**
     * return endY
     */
    protected int drawExtraText(R recipe, int y, GuiGraphics graphics) {
        return y;
    }

    protected static void addIngredient(IIngredientBuilder builder, Layout.SlotInfo slot,
        IProcessingObject ingredient) {
        switch (ingredient) {
            case StackIngredient<?> stackIngredient when stackIngredient.type() == PortType.ITEM ->
                builder.itemInput(slot, (ItemStack) stackIngredient.stack());
            case ItemsIngredient item -> {
                if (item.amount <= 0) {
                    builder.itemNotConsumedInput(slot, List.of(item.ingredient.getItems()));
                } else {
                    var items = Arrays.stream(item.ingredient.getItems())
                        .map(stack -> StackHelper.copyWithCount(stack, item.amount))
                        .toList();
                    builder.itemInput(slot, items);
                }
            }
            case StackIngredient<?> stackIngredient when stackIngredient.type() == PortType.FLUID ->
                builder.fluidInput(slot, (FluidStack) stackIngredient.stack());
            case StackResult<?> stackResult when stackResult.type() == PortType.ITEM ->
                builder.itemOutput(slot, (ItemStack) stackResult.stack(), stackResult.rate());
            case StackResult<?> stackResult when stackResult.type() == PortType.FLUID ->
                builder.fluidOutput(slot, (FluidStack) stackResult.stack(), stackResult.rate());
            default -> throw new IllegalArgumentException("Unknown processing ingredient type %s"
                .formatted(ingredient.getClass()));
        }
    }

    @Override
    protected void drawExtra(R recipe, ICategoryDrawHelper helper,
        IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        helper.drawProgressBar(graphics, (int) recipe.workTicks);
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        var total = recipe.power * recipe.workTicks;
        var duration = DOUBLE_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).displayName();

        y = drawExtraText(recipe, y, graphics);

        y = drawTextLine(graphics, tr("total", total), y);
        y = drawTextLine(graphics, tr("power", recipe.power, voltage), y);
        drawTextLine(graphics, tr("duration", duration), y);
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

    @Override
    protected void extraLayout(R recipe, IRecipeLayoutBuilder builder) {
        // register as OUTPUT so you can use the shortcut R to see it.
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(RecipeMarker.TYPE, new RecipeMarker(recipe.loc()));
    }

    protected void addTechIngredient(IRecipeLayoutBuilder builder, RecipeIngredientRole role,
        int x, int y, ResourceLocation loc) {
        builder.addSlot(role, x + 1 + xOffset, y + 1)
            .addIngredient(TechIngredient.TYPE, new TechIngredient(loc))
            .setCustomRenderer(TechIngredient.TYPE, TechIngredientRenderer.INSTANCE);
    }

    protected void addRequiredTech(IRecipeLayoutBuilder builder, Collection<ResourceLocation> techs) {
        var x = ClientUtil.getFont().width(tr("requiredTech")) + SPACING - layout.getXOffset();
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        for (var tech : techs) {
            addTechIngredient(builder, RecipeIngredientRole.OUTPUT, x, y, tech);
            x += TECH_SIZE;
        }
    }
}
