package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllMultiblocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.util.I18n;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceCategory extends ProcessingCategory<BlastFurnaceRecipe> {
    public BlastFurnaceCategory() {
        super(AllRecipes.BLAST_FURNACE, AllLayouts.BLAST_FURNACE, AllMultiblocks.BLAST_FURNACE.get());
    }

    @Override
    protected int getExtraHeight() {
        return super.getExtraHeight() + FONT_HEIGHT + SPACING;
    }

    @Override
    protected int drawExtraText(BlastFurnaceRecipe recipe, int y, PoseStack stack) {
        var text = I18n.tr("tinactory.jei.processing.temperature",
            NUMBER_FORMAT.format(recipe.temperature));
        return drawTextLine(stack, text, y);
    }
}
