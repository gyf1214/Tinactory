package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.gui.client.RenderUtil;
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

    private void renderFluid(PoseStack poseStack, FluidStack stack, int x, int y) {
        RenderUtil.renderGhostFluid(poseStack, stack, new Rect(x, y, 16, 16), getBlitOffset());
    }

    private void renderIngredient(PoseStack poseStack, IProcessingObject ingredient, int x, int y) {
        RenderUtil.renderIngredient(ingredient,
            stack -> RenderUtil.renderGhostItem(poseStack, stack, x, y),
            stack -> renderFluid(poseStack, stack, x, y));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (var ingredient : ingredients) {
            var slot = ingredient.slot();
            var slotType = ingredient.slot().type();
            if (slotType == SlotType.NONE) {
                continue;
            }
            var x = slot.x() + rect.x() + 1;
            var y = slot.y() + rect.y() + 1;
            renderIngredient(poseStack, ingredient.val(), x, y);
        }
    }

    public void clear() {
        ingredients.clear();
    }

    public void addIngredient(Layout.SlotInfo slot, IProcessingObject ingredient) {
        ingredients.add(new Layout.SlotWith<>(slot, ingredient));
    }
}
