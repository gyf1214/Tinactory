package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.ResearchBenchPlugin;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreenHandler implements IGuiContainerHandler<MenuScreen> {
    @Override
    public @Nullable Object getIngredientUnderMouse(MenuScreen screen, double mouseX, double mouseY) {
        var hovered = screen.getHovered((int) mouseX, (int) mouseY);
        if (hovered.isEmpty()) {
            return null;
        }
        if (hovered.get() instanceof FluidSlot slot) {
            var stack = slot.getFluidStack();
            return stack.isEmpty() ? null : stack;
        } else if (ResearchBenchPlugin.isHoveringTech(hovered.get())) {
            return TechManager.localTeam()
                .flatMap(ITeamProfile::getTargetTech)
                .map(tech -> new TechWrapper(tech.getLoc()))
                .orElse(null);
        } else if (screen instanceof NetworkControllerScreen && TechPanel.isHoveringTech(hovered.get())) {
            return TechPanel.getHoveredTech(hovered.get(), mouseX)
                .map(tech -> new TechWrapper(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }
}
