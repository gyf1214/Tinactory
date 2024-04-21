package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.FluidEventPacket;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.FLUID_CLICK;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends MenuWidget {
    private static final int HIGHLIGHT_COLOR = 0x80FFFFFF;

    private final int tank;
    private final int syncSlot;

    public FluidSlot(Menu<?> menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
    }

    private FluidStack getFluidStack() {
        return menu.getSyncPacket(syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        var fluidStack = getFluidStack();
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
    protected boolean canClick(int button) {
        return (button == 0 || button == 1) && !menu.getCarried().isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        menu.triggerEvent(FLUID_CLICK, (containerId, eventId) ->
                new FluidEventPacket(containerId, eventId, tank, button));
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderUtil.renderFluid(poseStack, getFluidStack(), rect, getBlitOffset());

        if (isHovering(mouseX, mouseY)) {
            RenderSystem.colorMask(true, true, true, false);
            RenderUtil.fill(poseStack, rect, HIGHLIGHT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
