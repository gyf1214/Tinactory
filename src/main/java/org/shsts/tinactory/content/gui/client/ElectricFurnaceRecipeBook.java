package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnaceRecipeBook extends AbstractRecipeBook<SmeltingRecipe> {
    private final Layout.SlotInfo inputSlot;
    private final Layout.SlotInfo outputSlot;

    public ElectricFurnaceRecipeBook(MenuScreen<? extends Menu<?, ?>> screen, Layout layout) {
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
        // TODO
        return Optional.of(List.of(I18n.raw(recipe.getId().toString())));
    }

    @Override
    protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
                                SmeltingRecipe recipe, Rect rect, int z) {
        int x = rect.x() + 2;
        int y = rect.y() + 2;
        RenderUtil.renderItem(recipe.getResultItem(), x, y);
    }
}
