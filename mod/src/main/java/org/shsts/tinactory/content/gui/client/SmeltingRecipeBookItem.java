package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.IRenderDescriptor;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmeltingRecipeBookItem implements IRecipeBookItem {
    private final SmeltingRecipe recipe;
    private final int inputPort;
    private final int outputPort;

    public SmeltingRecipeBookItem(SmeltingRecipe recipe, int inputPort, int outputPort) {
        this.recipe = recipe;
        this.inputPort = inputPort;
        this.outputPort = outputPort;
    }

    public SmeltingRecipe recipe() {
        return recipe;
    }

    @Override
    public ResourceLocation loc() {
        return recipe.getId();
    }

    @Override
    public boolean isMarker() {
        return false;
    }

    @Override
    public void select(Layout layout, BiConsumer<Layout.SlotInfo, IProcessingObject> ingredientCons) {
        var inputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == inputPort)
            .findFirst().orElseThrow();
        var outputSlot = layout.slots.stream()
            .filter(slot -> slot.port() == outputPort)
            .findFirst().orElseThrow();
        var ingredient = ItemsIngredient.of(recipe.getIngredients().get(0), 1);
        var result = ProcessingHelper.itemResult(1d, recipe.getResultItem());
        ingredientCons.accept(inputSlot, ingredient);
        ingredientCons.accept(outputSlot, result);
    }

    @Override
    public Optional<List<Component>> buttonToolTip() {
        return ClientUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .map(ClientUtil::itemTooltip);
    }

    @Override
    public IRenderDescriptor display() {
        return ClientUtil.selectItemFromItems(recipe.getIngredients().get(0))
            .<IRenderDescriptor>map(ItemRenderDescriptor::new)
            .orElse(EmptyRenderDescriptor.INSTANCE);
    }
}
