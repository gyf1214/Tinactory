package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingDisplayHelper {
    private ProcessingDisplayHelper() {}

    public static Optional<List<Component>> tooltip(IProcessingObject object) {
        if (object instanceof ProcessingIngredients.ItemIngredient item) {
            return Optional.of(ClientUtil.itemTooltip(item.stack()));
        } else if (object instanceof ItemsIngredient items) {
            return RenderUtil.selectItemFromItems(Arrays.asList(items.ingredient.getItems()))
                .map(ClientUtil::itemTooltip);
        } else if (object instanceof ProcessingIngredients.FluidIngredient fluid) {
            return Optional.of(ClientUtil.fluidTooltip(fluid.fluid(), false));
        } else if (object instanceof ProcessingResults.ItemResult item) {
            return Optional.of(ClientUtil.itemTooltip(item.stack));
        } else if (object instanceof ProcessingResults.FluidResult fluid) {
            return Optional.of(ClientUtil.fluidTooltip(fluid.stack, false));
        }
        return Optional.empty();
    }

    public static void render(IProcessingObject object, PoseStack poseStack, Rect rect, int z) {
        renderIngredient(object,
            stack -> RenderUtil.renderItem(stack, rect.x(), rect.y()),
            stack -> RenderUtil.renderFluid(poseStack, stack, rect.x(), rect.y(), z));
    }

    public static void renderIngredient(IProcessingObject object, Consumer<ItemStack> itemRenderer,
        Consumer<FluidStack> fluidRenderer) {
        if (object instanceof ProcessingIngredients.ItemIngredient item) {
            itemRenderer.accept(item.stack());
        } else if (object instanceof ItemsIngredient items) {
            RenderUtil.selectItemFromItems(Arrays.asList(items.ingredient.getItems())).ifPresent(itemRenderer);
        } else if (object instanceof ProcessingIngredients.FluidIngredient fluid) {
            fluidRenderer.accept(fluid.fluid());
        } else if (object instanceof ProcessingResults.ItemResult item) {
            itemRenderer.accept(item.stack);
        } else if (object instanceof ProcessingResults.FluidResult fluid) {
            fluidRenderer.accept(fluid.stack);
        }
    }
}
