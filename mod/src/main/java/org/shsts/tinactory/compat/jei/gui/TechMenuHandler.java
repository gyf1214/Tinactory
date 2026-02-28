package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredient;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.content.gui.client.TechScreen;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechMenuHandler extends MenuScreenHandler<TechScreen> {
    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered,
        double mouseX, double mouseY) {
        if (TechPanel.isHoveringTech(hovered)) {
            return TechPanel.getHoveredTech(hovered, mouseX)
                .map(tech -> new TechIngredient(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }
}
