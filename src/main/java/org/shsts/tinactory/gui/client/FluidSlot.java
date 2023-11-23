package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.sync.FluidSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    protected FluidStack getFluidStack() {
        return this.menu.getSyncPacket(this.syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        var fluidStack = this.getFluidStack();
        if (fluidStack.isEmpty() || fluidStack.getFluid() == null) {
            return Optional.empty();
        }

        var tooltip = new ArrayList<Component>();
        tooltip.add(fluidStack.getDisplayName());
        TranslatableComponent amountString = new TranslatableComponent("tinactory.tooltip.liquid",
                NUMBER_FORMAT.format(fluidStack.getAmount()));
        tooltip.add(amountString.withStyle(ChatFormatting.GRAY));

        return Optional.of(tooltip);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var fluidStack = this.getFluidStack();
        var fluid = fluidStack.getFluid();

        if (!fluidStack.isEmpty() && fluid != null) {
            var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
            var attribute = fluid.getAttributes();
            var sprite = atlas.apply(attribute.getStillTexture());
            RenderUtil.blitAtlas(poseStack, InventoryMenu.BLOCK_ATLAS, sprite,
                    attribute.getColor(), this.zIndex, this.rect);
        }

        if (this.isHovering(mouseX, mouseY)) {
            RenderSystem.colorMask(true, true, true, false);
            RenderUtil.fill(poseStack, this.rect, HIGHLIGHT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
