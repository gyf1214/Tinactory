package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.integration.gui.client.IViewAdapter;
import org.shsts.tinactory.integration.gui.client.MenuScreen;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuScreenHandler<M extends MenuScreen<?>> implements IGuiContainerHandler<M> {
    protected abstract @Nullable Object getIngredientHovered(IViewAdapter hovered, double mouseX, double mouseY);

    @Override
    public @Nullable Object getIngredientUnderMouse(M screen, double mouseX, double mouseY) {
        return screen.getHovered((int) mouseX, (int) mouseY)
            .map(widget -> getIngredientHovered(widget, mouseX, mouseY))
            .orElse(null);
    }
}
