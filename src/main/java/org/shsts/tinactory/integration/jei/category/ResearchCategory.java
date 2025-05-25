package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.tech.TechManager;

import java.util.List;

import static org.shsts.tinactory.content.gui.NetworkControllerPlugin.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchCategory extends ProcessingCategory<ResearchRecipe> {
    public ResearchCategory(Layout layout, Block icon) {
        super(AllRecipes.RESEARCH_BENCH, layout, icon);
    }

    @Override
    protected int extraHeight() {
        return super.extraHeight() + BUTTON_SIZE + FONT_HEIGHT + SPACING * 2;
    }

    @Override
    protected int drawExtraText(ResearchRecipe recipe, int y, PoseStack stack) {
        var tech = TechManager.client().techByKey(recipe.target);
        if (tech.isPresent()) {
            y = drawRequiredTechText(stack, tech.get().getDepends().isEmpty(), y);
            var text = tr("progress", NUMBER_FORMAT.format(recipe.progress),
                NUMBER_FORMAT.format(tech.get().getMaxProgress()));
            return drawTextLine(stack, text, y);
        } else {
            return y;
        }
    }

    @Override
    protected void extraLayout(ResearchRecipe recipe, IRecipeLayoutBuilder builder) {
        var rect = layout.images.get(0).rect();
        addTechIngredient(builder, RecipeIngredientRole.OUTPUT, rect.x(), rect.y(), recipe.target);
        var requiredTech = TechManager.client().techByKey(recipe.target)
            .map(tech -> tech.getDepends().stream().map(ITechnology::getLoc).toList())
            .orElse(List.of());
        addRequiredTech(builder, requiredTech);
    }
}
