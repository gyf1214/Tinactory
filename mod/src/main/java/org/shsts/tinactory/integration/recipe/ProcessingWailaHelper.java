package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import java.util.List;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingWailaHelper {
    private ProcessingWailaHelper() {}

    public static void appendElement(List<IElement> line, IProcessingObject object,
        BiConsumer<List<IElement>, ItemStack> itemAppender, BiConsumer<List<IElement>, FluidStack> fluidAppender) {
        if (object instanceof ProcessingIngredients.ItemIngredient item) {
            itemAppender.accept(line, item.stack());
        } else if (object instanceof ProcessingIngredients.FluidIngredient fluid) {
            fluidAppender.accept(line, fluid.fluid());
        } else if (object instanceof ProcessingResults.ItemResult item) {
            itemAppender.accept(line, item.stack);
        } else if (object instanceof ProcessingResults.FluidResult fluid) {
            fluidAppender.accept(line, fluid.stack);
        }
    }
}
