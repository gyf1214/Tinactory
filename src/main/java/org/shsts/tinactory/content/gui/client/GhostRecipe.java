package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GhostRecipe extends ContainerWidget {
    private final int syncSlot;

    @Nullable
    private ProcessingRecipe<?> currentRecipe = null;

    public GhostRecipe(ContainerMenu<?> menu, Rect rect, int syncSlot) {
        super(menu, rect);
        this.syncSlot = syncSlot;
    }

    private void updateRecipe() {
        var name = this.menu.getSyncPacket(this.syncSlot, ContainerSyncPacket.StringHolder.class)
                .map(ContainerSyncPacket.Holder::getData)
                .orElse("");
        var currentName = this.currentRecipe != null ? this.currentRecipe.getId().toString() : "";
        if (currentName.equals(name)) {
            return;
        }
        if (name.isEmpty()) {
            this.currentRecipe = null;
        } else {
            var recipeManager = ClientUtil.getRecipeManager();
            this.currentRecipe = (ProcessingRecipe<?>) recipeManager.byKey(new ResourceLocation(name))
                    .orElse(null);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.updateRecipe();
        if (this.currentRecipe == null) {
            return;
        }

    }
}
