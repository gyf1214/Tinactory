package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnaceRecipeBook extends AbstractRecipeBook<SmeltingRecipe> {
    private final Layout.SlotInfo inputSlot;
    private final Layout.SlotInfo outputSlot;

    public ElectricFurnaceRecipeBook(MenuScreen1<? extends Menu<?, ?>> screen, Layout layout) {
        super(screen, layout.getXOffset());
        this.inputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == 0)
            .findFirst().orElseThrow();
        this.outputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == 1)
            .findFirst().orElseThrow();
    }

    @Override
    protected void doRefreshRecipes() {
        var recipeManager = ClientUtil.getRecipeManager();
        recipeManager.getAllRecipesFor(RecipeType.SMELTING)
            .forEach(r -> recipes.put(r.getId(), r));
    }

    @Override
    protected int compareRecipes(SmeltingRecipe r1, SmeltingRecipe r2) {
        return r1.getId().compareNamespaced(r2.getId());
    }

    @Override
    protected void selectRecipe(SmeltingRecipe recipe) {
        var ingredient = ProcessingIngredients.ItemsIngredientBase.of(recipe.getIngredients().get(0), 1);
        var result = new ProcessingResults.ItemResult(1d, recipe.getResultItem());
        ghostRecipe.addIngredient(inputSlot, ingredient);
        ghostRecipe.addIngredient(outputSlot, result);
    }

    @Override
    protected Optional<List<Component>> buttonToolTip(SmeltingRecipe recipe) {
        return RenderUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .map(ClientUtil::itemTooltip);
    }

    @Override
    protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
        SmeltingRecipe recipe, Rect rect, int z) {
        RenderUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .ifPresent(item -> RenderUtil.renderItem(item, rect.x() + 2, rect.y() + 2));
    }
}
