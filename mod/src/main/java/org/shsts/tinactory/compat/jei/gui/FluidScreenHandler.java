package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.logistics.StackHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidScreenHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
    private @Nullable Object getIngredientHovered(Widget hovered) {
        if (hovered instanceof FluidSlot slot) {
            var stack = slot.getFluidStack();
            return stack.isEmpty() ? null : stack;
        }
        return null;
    }

    @Override
    public @Nullable Object getIngredientUnderMouse(AbstractContainerScreen<?> screen,
        double mouseX, double mouseY) {
        var slot = screen.getSlotUnderMouse();
        if (slot != null) {
            var item = slot.getItem();
            var fluid = StackHelper.getFluidHandlerFromItem(item)
                .map(h -> h.getFluidInTank(0))
                .orElse(FluidStack.EMPTY);
            if (!fluid.isEmpty()) {
                return fluid;
            }
        }

        if (screen instanceof MenuScreen<?> menuScreen) {
            return menuScreen.getHovered((int) mouseX, (int) mouseY)
                .map(this::getIngredientHovered)
                .orElse(null);
        }
        return null;
    }
}
