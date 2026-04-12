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
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.recipe.ProcessingStackHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingDisplayHelper {
    private ProcessingDisplayHelper() {}

    public static Optional<List<Component>> tooltip(IProcessingObject object) {
        if (object instanceof ItemsIngredient items) {
            return RenderUtil.selectItemFromItems(Arrays.asList(items.ingredient.getItems()))
                .map(ClientUtil::itemTooltip);
        }
        return ProcessingStackHelper.itemStack(object).map(ClientUtil::itemTooltip)
            .or(() -> ProcessingStackHelper.fluidStack(object)
                .map(fluid -> ClientUtil.fluidTooltip(fluid, false)));
    }

    public static void render(IProcessingObject object, PoseStack poseStack, Rect rect, int z) {
        renderIngredient(object,
            stack -> RenderUtil.renderItem(stack, rect.x(), rect.y()),
            stack -> RenderUtil.renderFluid(poseStack, stack, rect.x(), rect.y(), z));
    }

    public static void renderIngredient(IProcessingObject object, Consumer<ItemStack> itemRenderer,
        Consumer<FluidStack> fluidRenderer) {
        if (object instanceof ItemsIngredient items) {
            RenderUtil.selectItemFromItems(Arrays.asList(items.ingredient.getItems())).ifPresent(itemRenderer);
        } else if (ProcessingStackHelper.itemStack(object).isPresent()) {
            itemRenderer.accept(ProcessingStackHelper.itemStack(object).orElseThrow());
        } else {
            ProcessingStackHelper.fluidStack(object).ifPresent(fluidRenderer);
        }
    }
}
