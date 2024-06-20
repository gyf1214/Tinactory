package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends MenuWidget {
    public static final int HIGHLIGHT_COLOR = 0x80FFFFFF;

    private final int tank;
    private final int syncSlot;

    public FluidSlot(Menu<?, ?> menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
    }

    public FluidStack getFluidStack() {
        return menu.getSyncPacket(syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var stack = getFluidStack();
        if (stack.isEmpty() || stack.getFluid() == null) {
            return Optional.empty();
        }
        return Optional.of(RenderUtil.fluidTooltip(stack));
    }

    @Override
    protected boolean canClick(int button) {
        return (button == 0 || button == 1) && !menu.getCarried().isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        menu.triggerEvent(MenuEventHandler.FLUID_SLOT_CLICK, (containerId, eventId) ->
                new SlotEventPacket(containerId, eventId, tank, button));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.renderFluidWithDecoration(poseStack, getFluidStack(), rect, getBlitOffset());
        if (isHovering(mouseX, mouseY)) {
            RenderSystem.colorMask(true, true, true, false);
            RenderUtil.fill(poseStack, rect, HIGHLIGHT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
