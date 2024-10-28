package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
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
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

import static org.shsts.tinactory.content.gui.client.TechPanel.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.DOUBLE_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingCategory extends RecipeCategory<ProcessingRecipe, ProcessingMenu> {
    private static final int EXTRA_HEIGHT = FONT_HEIGHT * 3 + SPACING * 2 + SLOT_SIZE / 2;

    public ProcessingCategory(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType,
                              Layout layout, Block icon) {
        super(recipeType, layout, Ingredient.of(AllTags.machineTag(recipeType)),
                new ItemStack(icon), ProcessingMenu.class);
    }

    @Override
    protected ComposeDrawable.Builder buildBackground(ComposeDrawable.Builder builder,
                                                      IGuiHelper helper, int xOffset) {
        var extraHeight = EXTRA_HEIGHT;
        if (AssemblyRecipe.class.isAssignableFrom(recipeType.clazz)) {
            extraHeight += BUTTON_SIZE + SPACING;
        } else if (ResearchRecipe.class.isAssignableFrom(recipeType.clazz)) {
            extraHeight += BUTTON_SIZE + FONT_HEIGHT + SPACING * 2;
        }
        return super.buildBackground(builder, helper, xOffset)
                .add(helper.createBlankDrawable(WIDTH, extraHeight), 0, layout.rect.endY());
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
    protected void drawExtra(ProcessingRecipe recipe, IDrawHelper helper, IRecipeSlotsView recipeSlotsView,
                             PoseStack stack, double mouseX, double mouseY) {
        helper.drawProgressBar(stack, (int) recipe.workTicks);
        var y = layout.rect.endY() + SLOT_SIZE / 2;
        var total = recipe.power * recipe.workTicks;
        var duration = DOUBLE_FORMAT.format((double) recipe.workTicks / 20d);
        var voltage = Voltage.fromValue(recipe.voltage).id.toUpperCase();

        if (recipe instanceof AssemblyRecipe recipe1) {
            y = drawRequiredText(stack, recipe1.requiredTech.isEmpty(), y);
        } else if (recipe instanceof ResearchRecipe recipe1) {
            var tech = TechManager.client().techByKey(recipe1.target);
            if (tech.isPresent()) {
                y = drawRequiredText(stack, tech.get().getDepends().isEmpty(), y);
                var text = I18n.tr("tinactory.jei.processing.progress",
                        NUMBER_FORMAT.format(recipe1.progress),
                        NUMBER_FORMAT.format(tech.get().maxProgress));
                y = drawTextLine(stack, text, y);
            }
        }
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.total", total), y);
        y = drawTextLine(stack, I18n.tr("tinactory.jei.processing.power", recipe.power, voltage), y);
        drawTextLine(stack, I18n.tr("tinactory.jei.processing.duration", duration), y);
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
            builder.ratedItem(slot, item.stack, item.rate);
        } else if (ingredient instanceof ProcessingResults.FluidResult fluid) {
            builder.ratedFluid(slot, fluid.stack, fluid.rate);
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

        if (recipe instanceof ResearchRecipe recipe1) {
            var rect = layout.images.get(0).rect();
            var slot = new Layout.SlotInfo(0, rect.x(), rect.y(), 0, SlotType.NONE);
            builder.addIngredients(slot, RecipeIngredientRole.OUTPUT, TechIngredientType.INSTANCE,
                    List.of(new TechWrapper(recipe1.target)));
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
        var x = ClientUtil.getFont().width(I18n.tr("tinactory.jei.processing.requiredTech"))
                + SPACING - layout.getXOffset();
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
            var slot = new Layout.SlotInfo(0, x, y, 0, SlotType.NONE);
            builder.addIngredients(slot, RecipeIngredientRole.INPUT, TechIngredientType.INSTANCE,
                    List.of(new TechWrapper(loc)));
            x += BUTTON_SIZE;
        }
    }

    @Override
    protected boolean canTransfer(ProcessingMenu menu, ProcessingRecipe recipe) {
        return AllCapabilities.PROCESSOR.tryGet(menu.blockEntity)
                .map(p -> p instanceof MachineProcessor<?> processor &&
                        processor.recipeType == recipe.getType())
                .orElse(false);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // TODO: auto transfer of processing recipe is too buggy, disable now
    }
}
