package org.shsts.tinactory.integration.jei.ingredient;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;

import static org.shsts.tinactory.content.gui.client.TechPanel.BUTTON_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientRenderer implements IIngredientRenderer<TechWrapper> {
    private static final Rect RECT = new Rect(0, 0, BUTTON_SIZE, BUTTON_SIZE);

    private TechIngredientRenderer() {}

    @Override
    public void render(PoseStack poseStack, TechWrapper ingredient) {
        var tech = TechManager.client().techByKey(ingredient.loc());
        if (tech.isEmpty()) {
            return;
        }
        var team = TechManager.localTeam().orElse(null);
        TechPanel.renderTechButton(poseStack, 0, RECT, team, tech.get(), false);
    }

    @Override
    public List<Component> getTooltip(TechWrapper ingredient, TooltipFlag tooltipFlag) {
        return List.of(I18n.tr(ITechnology.getDescriptionId(ingredient.loc())));
    }

    @Override
    public int getWidth() {
        return BUTTON_SIZE;
    }

    @Override
    public int getHeight() {
        return BUTTON_SIZE;
    }

    public static final TechIngredientRenderer INSTANCE = new TechIngredientRenderer();
}
