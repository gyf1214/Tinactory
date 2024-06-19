package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
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

    private FluidStack getFluidStack() {
        return menu.getSyncPacket(syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var fluidStack = getFluidStack();
        if (fluidStack.isEmpty() || fluidStack.getFluid() == null) {
            return Optional.empty();
        }

        var tooltip = new ArrayList<Component>();
        tooltip.add(fluidStack.getDisplayName());
        var amountString = I18n.tr("tinactory.tooltip.liquid",
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
        menu.triggerEvent(MenuEventHandler.FLUID_SLOT_CLICK, (containerId, eventId) ->
                new SlotEventPacket(containerId, eventId, tank, button));
    }

    public String getAmountString(int amount) {
        if (amount < 1000) {
            return String.valueOf(amount);
        } else if (amount < 1000000) {
            return amount / 1000 + "B";
        } else {
            return amount / 1000000 + "k";
        }
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var stack = getFluidStack();
        RenderUtil.renderFluid(poseStack, stack, rect, getBlitOffset());

        if (!stack.isEmpty()) {
            var s = getAmountString(stack.getAmount());
            var font = ClientUtil.getFont();
            var x = rect.endX() + 1 - font.width(s);
            var y = rect.endY() + 2 - font.lineHeight;
            font.drawShadow(poseStack, s, x, y, 0xFFFFFFFF);
        }

        if (isHovering(mouseX, mouseY)) {
            RenderSystem.colorMask(true, true, true, false);
            RenderUtil.fill(poseStack, rect, HIGHLIGHT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
        }
    }
}
