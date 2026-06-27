package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

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
    public void select(Layout layout, BiConsumer<Layout.SlotInfo, IProcessingObject> ingredientCons) {
        layout.getProcessingInputs(recipe).forEach(x -> ingredientCons.accept(x.slot(), x.val()));
        layout.getProcessingOutputs(recipe).forEach(x -> ingredientCons.accept(x.slot(), x.val()));
    }

    @Override
    public Optional<List<Component>> buttonToolTip() {
        return recipe.tooltip();
    }

    @Override
    public IRenderDescriptor display() {
        return recipe.display();
    }
}
