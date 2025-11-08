package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ProcessingRecipeBookItem(ProcessingRecipe recipe) implements IRecipeBookItem {
    @Override
    public ResourceLocation loc() {
        return recipe.loc();
    }

    @Override
    public boolean isMarker() {
        return recipe instanceof MarkerRecipe;
    }

    @Override
    public void select(Layout layout, GhostRecipe ghostRecipe) {
        layout.getProcessingInputs(recipe).forEach(ghostRecipe::addIngredient);
        layout.getProcessingOutputs(recipe).forEach(ghostRecipe::addIngredient);
    }

    @Override
    public Optional<List<Component>> buttonToolTip() {
        return recipe.getDescription();
    }

    @Override
    public void render(PoseStack poseStack, Rect rect, int z) {
        recipe.getDisplay().getValue().render(poseStack, rect, z);
    }
}
