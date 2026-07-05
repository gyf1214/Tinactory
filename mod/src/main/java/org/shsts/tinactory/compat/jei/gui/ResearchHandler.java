package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.compat.jei.ingredient.TechIngredient;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.integration.gui.client.IViewAdapter;
import org.shsts.tinactory.integration.tech.TechManagers;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchHandler extends MenuScreenHandler<ResearchBenchScreen> {
    @Override
    protected Optional<ITypedIngredient<?>> getIngredientHovered(IViewAdapter hovered,
        double mouseX, double mouseY) {
        if (ResearchBenchScreen.isHoveringTech(hovered)) {
            return TechManagers.localTeam()
                .flatMap(ITeamProfile::getTargetTech)
                .map(tech -> TypedIngredient.createUnvalidated(
                    TechIngredient.TYPE, new TechIngredient(tech.loc())));
        }
        return Optional.empty();
    }
}
