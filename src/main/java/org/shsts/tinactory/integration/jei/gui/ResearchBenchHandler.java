package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchBenchHandler extends MenuScreenHandler<ResearchBenchScreen> {
    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered,
        double mouseX, double mouseY) {
        if (ResearchBenchScreen.isHoveringTech(hovered)) {
            return TechManager.localTeam()
                .flatMap(ITeamProfile::getTargetTech)
                .map(tech -> new TechWrapper(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }
}
