package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.IRecipeBookItemBase;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmeltingRecipeBookItem implements IRecipeBookItemBase {
    private final ResourceLocation loc;
    private final SmeltingRecipe recipe;
    private final int inputPort;
    private final int outputPort;

    public SmeltingRecipeBookItem(RecipeHolder<SmeltingRecipe> recipe, int inputPort, int outputPort) {
        this.loc = recipe.id();
        this.recipe = recipe.value();
        this.inputPort = inputPort;
        this.outputPort = outputPort;
    }

    public SmeltingRecipe recipe() {
        return recipe;
    }

    @Override
    public ResourceLocation loc() {
        return loc;
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
        var ingredient = ItemsIngredient.of(recipe.getIngredients().getFirst(), 1);
        var result = ProcessingHelper.itemResult(1d, recipe.getResultItem(ClientUtil.registryAccess()));
        ingredientCons.accept(inputSlot, ingredient);
        ingredientCons.accept(outputSlot, result);
    }

    @Override
    public Optional<List<Component>> buttonToolTip() {
        return ClientUtil.selectItemFromItems(recipe.getIngredients().getFirst())
            .map(ClientUtil::itemTooltip);
    }

    @Override
    public IRenderDescriptor display() {
        return ClientUtil.selectItemFromItems(recipe.getIngredients().getFirst())
            .<IRenderDescriptor>map(ItemRenderDescriptor::new)
            .orElse(EmptyRenderDescriptor.INSTANCE);
    }
}
