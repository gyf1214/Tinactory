package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerHandler extends MenuScreenHandler<NetworkControllerScreen> {
    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered,
        double mouseX, double mouseY) {
        if (TechPanel.isHoveringTech(hovered)) {
            return TechPanel.getHoveredTech(hovered, mouseX)
                .map(tech -> new TechWrapper(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }
}
