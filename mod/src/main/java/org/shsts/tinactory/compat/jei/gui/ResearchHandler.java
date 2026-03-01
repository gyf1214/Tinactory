package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredient;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.core.tech.TechManager;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchHandler extends MenuScreenHandler<ResearchBenchScreen> {
    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered,
        double mouseX, double mouseY) {
        if (ResearchBenchScreen.isHoveringTech(hovered)) {
            return TechManager.localTeam()
                .flatMap(ITeamProfile::getTargetTech)
                .map(tech -> new TechIngredient(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }
}
