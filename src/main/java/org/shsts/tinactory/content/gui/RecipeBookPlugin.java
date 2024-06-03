package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.GhostRecipe;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.client.ProcessingRecipeBook;
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
public abstract class RecipeBookPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    protected final Layout layout;
    protected final int buttonY;

    @OnlyIn(Dist.CLIENT)
    private MachineRecipeBook<?> recipeBook = null;

    public RecipeBookPlugin(M menu, Layout layout) {
        this.layout = layout;
        this.buttonY = menu.getHeight() - 18;
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract MachineRecipeBook<?> createRecipeBook(MenuScreen<M> screen);

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        recipeBook = createRecipeBook(screen);
        screen.addPanel(recipeBook);
        var rect = new Rect(layout.getXOffset(), 0, 0, 0);
        screen.addWidget(rect, new GhostRecipe(screen.getMenu(), layout));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onScreenRemoved() {
        if (recipeBook != null) {
            recipeBook.remove();
        }
    }

    public static <M extends Menu<?, M>> Function<M, IMenuPlugin<M>>
    processing(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType, Layout layout) {
        return menu -> new RecipeBookPlugin<>(menu, layout) {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected MachineRecipeBook<?> createRecipeBook(MenuScreen<M> screen) {
                return new ProcessingRecipeBook(screen, recipeType.get(), 0, buttonY);
            }
        };
    }
}
