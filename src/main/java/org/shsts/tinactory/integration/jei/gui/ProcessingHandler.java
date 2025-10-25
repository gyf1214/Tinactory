package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.SmeltingRecipeBookItem;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.integration.jei.ingredient.RecipeMarker;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingHandler extends MenuScreenHandler<ProcessingScreen> {
    private Object getRecipeBookIngredient(IRecipeBookItem item) {
        if (item instanceof SmeltingRecipeBookItem smelting) {
            return smelting.recipe().getResultItem();
        }
        return new RecipeMarker(item.loc());
    }

    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered, double mouseX, double mouseY) {
        return MachineRecipeBook.getHoveredRecipe(hovered)
            .map(this::getRecipeBookIngredient)
            .orElse(null);
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(ProcessingScreen screen,
        double mouseX, double mouseY) {
        var menu = screen.menu();
        var layout = menu.layout();
        var block = menu.machineBlock();
        if (layout.progressBar == null || block.isEmpty()) {
            return Collections.emptyList();
        }

        var item = new ItemStack(block.get());
        if (!item.is(AllTags.MACHINE)) {
            return Collections.emptyList();
        }

        var rect = layout.progressBar.rect()
            .offset(layout.getXOffset() + MARGIN_X, MARGIN_TOP)
            .toRect2i();

        var area = new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return rect;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                var focus = focusFactory.createFocus(RecipeIngredientRole.CATALYST,
                    VanillaTypes.ITEM_STACK, item);
                recipesGui.show(focus);
            }
        };

        return List.of(area);
    }
}
