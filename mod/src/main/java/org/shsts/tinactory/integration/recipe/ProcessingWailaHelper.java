package org.shsts.tinactory.integration.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.ui.IElement;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.List;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingWailaHelper {
    private ProcessingWailaHelper() {}

    public static void appendElement(List<IElement> line, IProcessingObject object,
        BiConsumer<List<IElement>, ItemStack> itemAppender, BiConsumer<List<IElement>, FluidStack> fluidAppender) {
        ProcessingHelper.itemStack(object).ifPresentOrElse(
            item -> itemAppender.accept(line, item),
            () -> ProcessingHelper.fluidStack(object).ifPresent(fluid -> fluidAppender.accept(line, fluid))
        );
    }
}
