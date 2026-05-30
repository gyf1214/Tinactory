package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.client.MEPatternDraft;

import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.I18n.tr;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternTransferHandler<R> implements IRecipeTransferHandler<MEPatternTerminalMenu, R> {
    private final Class<R> recipeClass;
    private final Function<R, Optional<MEPatternDraft>> converter;
    private final IRecipeTransferHandlerHelper helper;

    @SuppressWarnings("unchecked")
    public MEPatternTransferHandler(Class<? extends R> recipeClass, Function<R, Optional<MEPatternDraft>> converter,
        IRecipeTransferHandlerHelper helper) {
        this.recipeClass = (Class<R>) recipeClass;
        this.converter = converter;
        this.helper = helper;
    }

    @Override
    public Class<MEPatternTerminalMenu> getContainerClass() {
        return MEPatternTerminalMenu.class;
    }

    @Override
    public Class<R> getRecipeClass() {
        return recipeClass;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(MEPatternTerminalMenu container, R recipe,
        IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        var draft = converter.apply(recipe);
        if (draft.isEmpty()) {
            return error("unsupportedRecipe");
        }
        if (!container.importRecipeDraft(draft.get(), doTransfer)) {
            return helper.createInternalError();
        }
        return null;
    }

    private IRecipeTransferError error(String key) {
        Component message = tr("tinactory.jei.pattern." + key);
        return helper.createUserErrorWithTooltip(message);
    }
}
