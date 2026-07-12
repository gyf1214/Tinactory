package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.util.ClientUtil.PERCENTAGE_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanCategory extends ProcessingCategory<CleanRecipe> {
    public CleanCategory(
        IRecipeType<CleanRecipe> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int extraHeight() {
        return super.extraHeight() + FONT_HEIGHT + SPACING;
    }

    @Override
    protected int drawExtraText(CleanRecipe recipe, int y, GuiGraphics graphics) {
        var text = tr("cleanness", PERCENTAGE_FORMAT.format(recipe.minCleanness),
            PERCENTAGE_FORMAT.format(recipe.maxCleanness));
        return drawTextLine(graphics, text, y);
    }
}
