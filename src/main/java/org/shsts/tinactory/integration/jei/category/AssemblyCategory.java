package org.shsts.tinactory.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.content.gui.client.TechPanel.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyCategory extends ProcessingCategory<AssemblyRecipe> {
    public AssemblyCategory(
        IRecipeType<? extends IRecipeBuilderBase<AssemblyRecipe>> recipeType,
        Layout layout, Block icon) {
        super(recipeType, layout, icon);
    }

    @Override
    protected int getExtraHeight() {
        return super.getExtraHeight() + BUTTON_SIZE + SPACING;
    }

    @Override
    protected int drawExtraText(AssemblyRecipe recipe, int y, PoseStack stack) {
        return drawRequiredTechText(stack, recipe.requiredTech.isEmpty(), y);
    }

    @Override
    protected void extraLayout(AssemblyRecipe recipe, IRecipeLayoutBuilder builder) {
        addRequiredTech(builder, recipe.requiredTech);
    }
}
