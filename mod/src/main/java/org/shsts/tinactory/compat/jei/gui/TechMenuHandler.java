package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredient;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.content.gui.client.TechScreen;
import org.shsts.tinactory.integration.gui.client.IViewAdapter;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechMenuHandler extends MenuScreenHandler<TechScreen> {
    @Override
    protected Optional<ITypedIngredient<?>> getIngredientHovered(IViewAdapter hovered,
        double mouseX, double mouseY) {
        if (TechPanel.isHoveringTech(hovered)) {
            return TechPanel.getHoveredTech(hovered, mouseX)
                .map(tech -> TypedIngredient.createUnvalidated(
                    TechIngredient.TYPE, new TechIngredient(tech.loc())));
        }
        return Optional.empty();
    }
}
