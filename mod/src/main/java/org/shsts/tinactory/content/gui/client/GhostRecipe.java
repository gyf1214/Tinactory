package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.MenuWidget;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GhostRecipe extends MenuWidget {
    private final List<Layout.SlotWith<? extends IProcessingObject>> ingredients = new ArrayList<>();

    public GhostRecipe(MenuBase menu) {
        super(menu);
    }

    private void renderIngredient(GuiGraphics graphics, IProcessingObject ingredient, int x, int y) {
        if (ingredient instanceof IProcessingDisplay display) {
            RenderUtil.renderGhostDescriptor(graphics, display.display(), new Rect(x, y, 16, 16));
        }
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (var ingredient : ingredients) {
            var slot = ingredient.slot();
            var slotType = ingredient.slot().type();
            if (slotType == SlotType.NONE) {
                continue;
            }
            var x = slot.x() + rect().x() + 1;
            var y = slot.y() + rect().y() + 1;
            renderIngredient(graphics, ingredient.val(), x, y);
        }
    }

    public void clear() {
        ingredients.clear();
    }

    public void addIngredient(Layout.SlotInfo slot, IProcessingObject ingredient) {
        ingredients.add(new Layout.SlotWith<>(slot, ingredient));
    }
}
