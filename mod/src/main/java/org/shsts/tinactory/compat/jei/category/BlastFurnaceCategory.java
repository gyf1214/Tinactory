package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.UnknownNullability;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnaceCategory extends ProcessingCategory<BlastFurnaceRecipe> {

    public BlastFurnaceCategory(IRecipeType<BlastFurnaceRecipe> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int extraHeight() {
        return super.extraHeight() + FONT_HEIGHT + SPACING;
    }

    @Override
    protected int drawExtraText(BlastFurnaceRecipe recipe, int y, @UnknownNullability GuiGraphics graphics) {
        var text = tr("temperature", NUMBER_FORMAT.format(recipe.temperature));
        return drawTextLine(graphics, text, y);
    }
}
