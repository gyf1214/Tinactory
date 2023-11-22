package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.sync.FluidSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends ContainerWidget {
    protected static final int HIGHLIGHT_COLOR = 0x80FFFFFF;

    protected final int syncSlot;

    public FluidSlot(ContainerMenu<?> menu, Rect rect, int syncSlot) {
        super(menu, rect, ContainerMenu.DEFAULT_Z_INDEX);
        this.syncSlot = syncSlot;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var fluidStack = this.menu.getSyncPacket(this.syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
        var fluid = fluidStack.getFluid();

        if (!fluidStack.isEmpty() && fluid != null) {
            var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            var attribute = fluid.getAttributes();
            var sprite = atlas.apply(attribute.getStillTexture());
            RenderUtil.blitAtlas(poseStack, InventoryMenu.BLOCK_ATLAS, sprite,
                    attribute.getColor(), this.zIndex, this.rect);
        }
    }
}
