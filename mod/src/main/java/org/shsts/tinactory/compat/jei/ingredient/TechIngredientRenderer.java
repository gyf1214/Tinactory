package org.shsts.tinactory.compat.jei.ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.TECH_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientRenderer implements IIngredientRenderer<TechIngredient> {
    private static final Rect RECT = new Rect(0, 0, TECH_SIZE, TECH_SIZE);

    private TechIngredientRenderer() {}

    @Override
    public void render(PoseStack poseStack, TechIngredient ingredient) {
        var tech = TechManager.client().techByKey(ingredient.loc());
        if (tech.isEmpty()) {
            return;
        }
        var team = TechManager.localTeam().orElse(null);
        TechPanel.renderTechButton(poseStack, 0, RECT, team, tech.get(), false);
    }

    @Override
    public List<Component> getTooltip(TechIngredient ingredient, TooltipFlag tooltipFlag) {
        return List.of(I18n.tr(ITechnology.getDescriptionId(ingredient.loc())));
    }

    @Override
    public int getWidth() {
        return TECH_SIZE;
    }

    @Override
    public int getHeight() {
        return TECH_SIZE;
    }

    public static final TechIngredientRenderer INSTANCE = new TechIngredientRenderer();
}
