package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.GhostRecipe;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeBookPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private final int syncSlot;
    private final RecipeType<? extends ProcessingRecipe<?>> recipeType;
    private final Layout layout;

    public RecipeBookPlugin(M menu, RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType,
                            Layout layout) {
        this.syncSlot = menu.addSyncSlot(MenuSyncPacket.LocHolder::new,
                be -> Machine.get(be).config.getLoc("targetRecipe").orElse(null));
        this.recipeType = recipeType.get();
        this.layout = layout;
    }

    public static <M extends Menu<?, M>> Function<M, IMenuPlugin<M>>
    builder(RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType, Layout layout) {
        return menu -> new RecipeBookPlugin<>(menu, recipeType, layout);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        screen.addPanel(new MachineRecipeBook(screen, syncSlot, recipeType, 0, 0));
        var rect = new Rect(layout.getXOffset(), 0, 0, 0);
        screen.addWidget(rect, new GhostRecipe(screen.getMenu(), syncSlot, layout));
    }
}
