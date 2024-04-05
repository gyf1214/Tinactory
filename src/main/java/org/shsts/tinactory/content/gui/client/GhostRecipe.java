package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;
import org.shsts.tinactory.core.logistics.SlotType;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.shsts.tinactory.core.util.GeneralUtil.optionalCastor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GhostRecipe extends ContainerWidget {
    private final int syncSlot;
    private final Layout layout;

    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    private final List<Layout.SlotWith<?>> ingredients = new ArrayList<>();
    private final ItemRenderer itemRenderer = ClientUtil.getItemRenderer();

    public GhostRecipe(ContainerMenu<?> menu, int syncSlot, Layout layout) {
        super(menu, Rect.ZERO.offset(layout.getXOffset(), 0));
        this.syncSlot = syncSlot;
        this.layout = layout;
    }

    private void updateRecipe() {
        var loc = menu.getSyncPacket(syncSlot, ContainerSyncPacket.LocHolder.class)
                .flatMap(ContainerSyncPacket.Holder::getData)
                .orElse(null);

        if (Objects.equals(loc, currentRecipeLoc)) {
            return;
        }
        currentRecipeLoc = loc;
        var recipe = Optional.ofNullable(loc)
                .flatMap(ClientUtil.getRecipeManager()::byKey)
                .flatMap(optionalCastor(ProcessingRecipe.class));

        ingredients.clear();
        recipe.map(layout::getProcessingInputs).ifPresent(ingredients::addAll);
        recipe.map(layout::getProcessingOutputs).ifPresent(ingredients::addAll);
    }

    private void renderItem(PoseStack poseStack, ItemStack stack, int x, int y) {
        itemRenderer.renderAndDecorateFakeItem(stack, x, y);
        RenderSystem.depthFunc(516);
        RenderUtil.fill(poseStack, new Rect(x, y, 16, 16), 0x808B8B8B);
        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
    }

    private void renderFluid(PoseStack poseStack, FluidStack stack, int x, int y) {
        var rect = new Rect(x, y, 16, 16);
        RenderUtil.renderFluid(poseStack, stack, rect, 0x80FFFFFF, zIndex);
    }

    private <I> void renderIngredient(PoseStack poseStack, I ingredient, int x, int y) {
        RenderUtil.renderIngredient(ingredient,
                stack -> renderItem(poseStack, stack, x, y),
                stack -> renderFluid(poseStack, stack, x, y));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateRecipe();

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
}
