package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockRecipeBook extends MachineRecipeBook {
    @SuppressWarnings("unchecked")
    private static Optional<RecipeType<? extends ProcessingRecipe>> getBlockRecipeType(
        ProcessingScreen screen) {
        var multiBlockInterface = (MultiBlockInterface) MACHINE
            .get(screen.menu().blockEntity());
        return multiBlockInterface.getRecipeType()
            .map($ -> (RecipeType<? extends ProcessingRecipe>) $);
    }

    public MultiBlockRecipeBook(ProcessingScreen screen, Layout layout) {
        super(screen, layout, getBlockRecipeType(screen).orElse(null));
    }
}
