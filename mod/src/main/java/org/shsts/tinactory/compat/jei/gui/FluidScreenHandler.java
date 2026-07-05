package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.shsts.tinactory.integration.gui.client.FluidSlot;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidScreenHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {
    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
        IClickableIngredientFactory builder, AbstractContainerScreen<?> screen,
        double mouseX, double mouseY) {
        var slot = screen.getSlotUnderMouse();
        if (slot != null) {
            var item = slot.getItem();
            var fluid = StackHelper.getFluidFromItem(item);
            if (!fluid.isEmpty()) {
                return builder.createBuilder(NeoForgeTypes.FLUID_STACK, fluid)
                    .buildWithArea(slot.x, slot.y, 16, 16);
            }
        }

        if (screen instanceof MenuScreen<?> menuScreen) {
            var hovered = menuScreen.getHovered((int) mouseX, (int) mouseY);
            if (hovered.isEmpty() || !(hovered.get() instanceof FluidSlot slot1)) {
                return Optional.empty();
            }
            var stack = slot1.getFluidStack();
            if (stack.isEmpty()) {
                return Optional.empty();
            }
            var rect = hovered.get().rect();
            return builder.createBuilder(NeoForgeTypes.FLUID_STACK, stack)
                .buildWithArea(rect.x(), rect.y(), rect.width(), rect.height());
        }
        return Optional.empty();
    }
}
