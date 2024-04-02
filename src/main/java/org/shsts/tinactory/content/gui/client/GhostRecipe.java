package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;
import org.shsts.tinactory.core.logistics.SlotType;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
    private List<Layout.SlotWith<IProcessingIngredient>> ingredients = List.of();

    public GhostRecipe(ContainerMenu<?> menu, int syncSlot, Layout layout) {
        super(menu, Rect.ZERO.offset(layout.getXOffset(), 0));
        this.syncSlot = syncSlot;
        this.layout = layout;
    }

    private void updateRecipe() {
        var loc = this.menu.getSyncPacket(this.syncSlot, ContainerSyncPacket.LocHolder.class)
                .flatMap(ContainerSyncPacket.Holder::getData)
                .orElse(null);

        if (Objects.equals(loc, this.currentRecipeLoc)) {
            return;
        }
        this.currentRecipeLoc = loc;

        this.ingredients = Optional.ofNullable(loc)
                .flatMap(ClientUtil.getRecipeManager()::byKey)
                .flatMap(optionalCastor(ProcessingRecipe.class))
                .map(this.layout::getProcessingInputs)
                .orElse(List.of());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.updateRecipe();

        var itemRenderer = ClientUtil.getItemRenderer();
        for (var input : this.ingredients) {
            var slot = input.slot();
            var slotType = input.slot().type();
            if (slotType == SlotType.NONE || slotType.output) {
                continue;
            }
            if (input.val() instanceof ProcessingIngredients.SimpleItemIngredient itemIngredient) {
                var x = slot.x() + rect.x() + 1;
                var y = slot.y() + rect.y() + 1;
                itemRenderer.renderAndDecorateFakeItem(itemIngredient.stack(), x, y);
                RenderSystem.depthFunc(516);
                RenderUtil.fill(poseStack, new Rect(x, y, 16, 16), 0x30FFFFFF);
                RenderSystem.depthFunc(515);
            }
        }
    }
}
