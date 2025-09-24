package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record SmeltingRecipeBookItem(SmeltingRecipe recipe) implements IRecipeBookItem {
    @Override
    public ResourceLocation loc() {
        return recipe.getId();
    }

    @Override
    public void select(Layout layout, GhostRecipe ghostRecipe) {
        var inputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == 0)
            .findFirst().orElseThrow();
        var outputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == 1)
            .findFirst().orElseThrow();
        var ingredient = ProcessingIngredients.ItemsIngredientBase.of(recipe.getIngredients().get(0), 1);
        var result = new ProcessingResults.ItemResult(1d, recipe.getResultItem());
        ghostRecipe.addIngredient(inputSlot, ingredient);
        ghostRecipe.addIngredient(outputSlot, result);
    }

    @Override
    public Optional<List<Component>> buttonToolTip() {
        return RenderUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .map(ClientUtil::itemTooltip);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick, Rect rect, int z) {
        RenderUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .ifPresent(item -> RenderUtil.renderItem(item, rect.x() + 2, rect.y() + 2));
    }
}
