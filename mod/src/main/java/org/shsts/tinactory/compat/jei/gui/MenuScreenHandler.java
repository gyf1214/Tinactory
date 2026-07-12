package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.integration.gui.client.IViewAdapter;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuScreenHandler<M extends MenuScreen<?>> implements IGuiContainerHandler<M> {
    protected abstract Optional<ITypedIngredient<?>> getIngredientHovered(IViewAdapter hovered,
        double mouseX, double mouseY);

    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
        IClickableIngredientFactory builder, M screen, double mouseX, double mouseY) {
        var hovered = screen.getHovered((int) mouseX, (int) mouseY);
        if (hovered.isEmpty()) {
            return Optional.empty();
        }
        var ingredient = getIngredientHovered(hovered.get(), mouseX, mouseY);
        if (ingredient.isEmpty()) {
            return Optional.empty();
        }
        var rect = hovered.get().rect();
        return builder.createBuilder(ingredient.get())
            .buildWithArea(rect.x(), rect.y(), rect.width(), rect.height());
    }
}
