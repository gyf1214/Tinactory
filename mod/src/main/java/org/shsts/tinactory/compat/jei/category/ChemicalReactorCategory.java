package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalReactorCategory extends AssemblyCategory<ChemicalReactorRecipe> {
    public ChemicalReactorCategory(
        IRecipeType<ChemicalReactorRecipe> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int extraHeight() {
        return super.extraHeight() + FONT_HEIGHT + SPACING;
    }

    @Override
    protected int drawExtraText(ChemicalReactorRecipe recipe, int y, GuiGraphics graphics) {
        y = super.drawExtraText(recipe, y, graphics);
        if (recipe.requireMultiblock) {
            drawTextLine(graphics, tr("requireLargeChemicalReactor"), y);
        }
        return y + FONT_HEIGHT + SPACING;
    }
}
