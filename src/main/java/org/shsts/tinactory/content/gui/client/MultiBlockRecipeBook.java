package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockRecipeBook extends MachineRecipeBook {
    @SuppressWarnings("unchecked")
    private static Optional<RecipeType<? extends ProcessingRecipe>> getBlockRecipeType(
        MenuScreen1<? extends ProcessingMenu> screen) {
        var multiBlockInterface = (MultiBlockInterface) AllCapabilities.MACHINE
            .get(screen.getMenu().blockEntity);
        return multiBlockInterface.getRecipeType()
            .map($ -> (RecipeType<? extends ProcessingRecipe>) $);
    }

    public MultiBlockRecipeBook(MenuScreen1<? extends ProcessingMenu> screen) {
        super(screen, getBlockRecipeType(screen).orElse(null));
    }
}
