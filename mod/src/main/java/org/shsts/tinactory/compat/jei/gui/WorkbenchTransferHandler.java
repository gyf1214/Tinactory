package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.WorkbenchTransferResult;
import org.shsts.tinactory.content.gui.sync.WorkbenchTransferEventPacket;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.WORKBENCH_TRANSFER;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class WorkbenchTransferHandler<R> implements IRecipeTransferHandler<WorkbenchMenu, R> {
    private final RecipeType<R> recipeType;
    private final IRecipeTransferHandlerHelper helper;

    private WorkbenchTransferHandler(RecipeType<R> recipeType, IRecipeTransferHandlerHelper helper) {
        this.recipeType = recipeType;
        this.helper = helper;
    }

    public static IRecipeTransferHandler<WorkbenchMenu, IEntry<ToolRecipe>> tool(RecipeType<IEntry<ToolRecipe>> type,
        IRecipeTransferHandlerHelper helper) {
        return new Tool(type, helper);
    }

    public static IRecipeTransferHandler<WorkbenchMenu, RecipeHolder<CraftingRecipe>> crafting(
        IRecipeTransferHandlerHelper helper) {
        return new Crafting(helper);
    }

    @Override
    public Class<WorkbenchMenu> getContainerClass() {
        return WorkbenchMenu.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MenuType<WorkbenchMenu>> getMenuType() {
        return Optional.of((MenuType<WorkbenchMenu>) AllMenus.WORKBENCH.get());
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(WorkbenchMenu container, R recipe, IRecipeSlotsView slotsView,
        Player player, boolean maxTransfer, boolean doTransfer) {
        var result = planTransfer(container, recipe, maxTransfer);
        if (result.code() == WorkbenchTransferResult.Code.MISSING_INPUT) {
            return helper.createUserErrorForMissingSlots(tr("jei.tooltip.error.recipe.transfer.missing"),
                missingSlots(slotsView, result.missingIndexes()));
        }
        if (result.code() == WorkbenchTransferResult.Code.INVENTORY_FULL) {
            return helper.createUserErrorWithTooltip(tr("jei.tooltip.error.recipe.transfer.inventory.full"));
        }
        if (result.code() == WorkbenchTransferResult.Code.UNSUPPORTED_RECIPE) {
            return helper.createInternalError();
        }
        if (doTransfer) {
            container.triggerEvent(WORKBENCH_TRANSFER,
                () -> new WorkbenchTransferEventPacket(recipeId(recipe), maxTransfer));
        }
        return null;
    }

    protected abstract WorkbenchTransferResult planTransfer(WorkbenchMenu menu, R recipe, boolean maxTransfer);

    protected abstract ResourceLocation recipeId(R recipe);

    protected abstract List<IRecipeSlotView> missingSlots(IRecipeSlotsView slotsView, List<Integer> missingIndexes);

    private static List<IRecipeSlotView> materialSlots(IRecipeSlotsView slotsView, List<Integer> missingIndexes) {
        var missing = new ArrayList<IRecipeSlotView>();
        var inputs = slotsView.getSlotViews(RecipeIngredientRole.INPUT);
        for (var index : missingIndexes) {
            if (index >= 1 && index <= 9 && index - 1 < inputs.size()) {
                missing.add(inputs.get(index - 1));
            }
        }
        return missing;
    }

    private static final class Tool extends WorkbenchTransferHandler<IEntry<ToolRecipe>> {
        private Tool(RecipeType<IEntry<ToolRecipe>> type, IRecipeTransferHandlerHelper helper) {
            super(type, helper);
        }

        @Override
        protected WorkbenchTransferResult planTransfer(WorkbenchMenu menu, IEntry<ToolRecipe> recipe,
            boolean maxTransfer) {
            return menu.planTransfer(recipe.get(), maxTransfer);
        }

        @Override
        protected ResourceLocation recipeId(IEntry<ToolRecipe> recipe) {
            return recipe.loc();
        }

        @Override
        protected List<IRecipeSlotView> missingSlots(IRecipeSlotsView slotsView, List<Integer> missingIndexes) {
            var missing = materialSlots(slotsView, missingIndexes);
            var catalysts = slotsView.getSlotViews(RecipeIngredientRole.CATALYST);
            for (var index : missingIndexes) {
                if (index >= 10 && index <= 18 && index - 10 < catalysts.size()) {
                    missing.add(catalysts.get(index - 10));
                }
            }
            return missing;
        }
    }

    private static final class Crafting extends WorkbenchTransferHandler<RecipeHolder<CraftingRecipe>> {
        private Crafting(IRecipeTransferHandlerHelper helper) {
            super(RecipeTypes.CRAFTING, helper);
        }

        @Override
        protected WorkbenchTransferResult planTransfer(WorkbenchMenu menu, RecipeHolder<CraftingRecipe> recipe,
            boolean maxTransfer) {
            return menu.planTransfer(recipe.value(), maxTransfer);
        }

        @Override
        protected ResourceLocation recipeId(RecipeHolder<CraftingRecipe> recipe) {
            return recipe.id();
        }

        @Override
        protected List<IRecipeSlotView> missingSlots(IRecipeSlotsView slotsView, List<Integer> missingIndexes) {
            return materialSlots(slotsView, missingIndexes);
        }
    }
}
