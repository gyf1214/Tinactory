package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.PERCENTAGE_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanCategory extends ProcessingCategory<CleanRecipe> {
    public CleanCategory(
        IRecipeType<? extends IRecipeBuilderBase<CleanRecipe>> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int getExtraHeight() {
        return super.getExtraHeight() + FONT_HEIGHT + SPACING;
    }

    @Override
    protected int drawExtraText(CleanRecipe recipe, int y, PoseStack stack) {
        var text = I18n.tr("tinactory.jei.processing.cleanness",
            PERCENTAGE_FORMAT.format(recipe.minCleanness),
            PERCENTAGE_FORMAT.format(recipe.maxCleanness));
        return drawTextLine(stack, text, y);
    }
}
