package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.WorkbenchTransferResult;
import org.shsts.tinactory.content.gui.sync.WorkbenchTransferEventPacket;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.WORKBENCH_TRANSFER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class WorkbenchTransferHandler implements IRecipeTransferHandler<WorkbenchMenu, IEntry<ToolRecipe>> {
    private final RecipeType<IEntry<ToolRecipe>> recipeType;
    private final IRecipeTransferHandlerHelper helper;

    public WorkbenchTransferHandler(RecipeType<IEntry<ToolRecipe>> recipeType, IRecipeTransferHandlerHelper helper) {
        this.recipeType = recipeType;
        this.helper = helper;
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
    public RecipeType<IEntry<ToolRecipe>> getRecipeType() {
        return recipeType;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(WorkbenchMenu container, IEntry<ToolRecipe> entry,
        IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        var result = container.planTransfer(entry.get(), maxTransfer);
        if (result.code() == WorkbenchTransferResult.Code.MISSING_INPUT) {
            var missing = new ArrayList<IRecipeSlotView>();
            var inputs = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
            var catalysts = recipeSlotsView.getSlotViews(RecipeIngredientRole.CATALYST);
            for (var index : result.missingIndexes()) {
                if (index >= 1 && index <= 9 && index - 1 < inputs.size()) {
                    missing.add(inputs.get(index - 1));
                } else if (index >= 10 && index <= 18 && index - 10 < catalysts.size()) {
                    missing.add(catalysts.get(index - 10));
                }
            }
            return helper.createUserErrorForMissingSlots(
                Component.translatable("jei.tooltip.error.recipe.transfer.missing"), missing);
        }
        if (result.code() == WorkbenchTransferResult.Code.INVENTORY_FULL) {
            return helper.createUserErrorWithTooltip(Component.literal("Not enough inventory space"));
        }
        if (doTransfer) {
            container.triggerEvent(WORKBENCH_TRANSFER,
                () -> new WorkbenchTransferEventPacket(entry.loc(), maxTransfer));
        }
        return null;
    }
}
