package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Menu.TECH_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyCategory<R extends AssemblyRecipe> extends ProcessingCategory<R> {
    public AssemblyCategory(
        IRecipeType<R> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int extraHeight() {
        return super.extraHeight() + TECH_SIZE + SPACING;
    }

    @Override
    protected int drawExtraText(R recipe, int y, GuiGraphics graphics) {
        return drawRequiredTechText(graphics, recipe.requiredTech.isEmpty(), y);
    }

    @Override
    protected void extraLayout(ResourceLocation loc, R recipe, IRecipeLayoutBuilder builder) {
        super.extraLayout(loc, recipe, builder);
        addRequiredTech(builder, recipe.requiredTech);
    }
}
