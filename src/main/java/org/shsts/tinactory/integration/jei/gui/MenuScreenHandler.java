package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuScreenHandler<M extends MenuScreen> implements IGuiContainerHandler<M> {
    protected abstract @Nullable Object getIngredientHovered(Widget hovered, double mouseX, double mouseY);

    @Override
    public @Nullable Object getIngredientUnderMouse(M screen, double mouseX, double mouseY) {
        var hovered = screen.getHovered((int) mouseX, (int) mouseY);
        return hovered.map(widget -> getIngredientHovered(widget, mouseX, mouseY))
            .orElse(null);
    }

    public static MenuScreenHandler<MenuScreen> fluid() {
        return new MenuScreenHandler<>() {
            @Override
            protected @Nullable Object getIngredientHovered(Widget hovered,
                double mouseX, double mouseY) {
                if (hovered instanceof FluidSlot slot) {
                    var stack = slot.getFluidStack();
                    return stack.isEmpty() ? null : stack;
                }
                return null;
            }
        };
    }
}
