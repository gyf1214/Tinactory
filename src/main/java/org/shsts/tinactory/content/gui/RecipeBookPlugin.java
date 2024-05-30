package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.GhostRecipe;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeBookPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private final RecipeType<? extends ProcessingRecipe> recipeType;
    private final Layout layout;

    @OnlyIn(Dist.CLIENT)
    private MachineRecipeBook machineRecipeBook = null;

    public RecipeBookPlugin(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType,
                            Layout layout) {
        this.recipeType = recipeType.get();
        this.layout = layout;
    }

    public static <M extends Menu<?, M>> Function<M, IMenuPlugin<M>>
    builder(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType, Layout layout) {
        return $ -> new RecipeBookPlugin<>(recipeType, layout);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        machineRecipeBook = new MachineRecipeBook(screen, recipeType, 0, 0);
        screen.addPanel(machineRecipeBook);
        var rect = new Rect(layout.getXOffset(), 0, 0, 0);
        screen.addWidget(rect, new GhostRecipe(screen.getMenu(), layout));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onScreenRemoved() {
        if (machineRecipeBook != null) {
            machineRecipeBook.remove();
        }
    }
}
