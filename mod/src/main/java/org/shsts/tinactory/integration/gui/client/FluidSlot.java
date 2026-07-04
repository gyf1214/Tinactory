package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.integration.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.FLUID_SLOT_CLICK;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends MenuWidget {
    private final int tank;
    private final int syncSlot;
    @Nullable
    private final String syncName;

    public FluidSlot(MenuBase menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
        this.syncName = null;
    }

    public FluidSlot(MenuBase menu, int tank, String syncName) {
        super(menu);
        this.tank = tank;
        this.syncSlot = 0;
        this.syncName = syncName;
    }

    public FluidStack getFluidStack() {
        if (syncName != null) {
            return menu.getSyncPacket(syncName, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
        } else {
            return menu.getSyncPacket(syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
        }
    }

    @Override
    public boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var stack = getFluidStack();
        if (stack.isEmpty() || stack.getFluid() == null) {
            return Optional.empty();
        }
        return Optional.of(ClientUtil.fluidTooltip(stack, true));
    }

    @Override
    protected boolean canClick(int button, double mouseX, double mouseY) {
        return (button == 0 || button == 1) && !menu.getCarried().isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        ClientUtil.playSound(SoundEvents.BUCKET_FILL);
        menu.triggerEvent(FLUID_SLOT_CLICK, () -> new SlotEventPacket(tank, button));
    }

    protected void renderSlot(GuiGraphics graphics, int mouseX, int mouseY) {
        var rect = requireRect();
        RenderUtil.renderFluidWithDecoration(graphics, getFluidStack(), rect);
        if (isHovered(mouseX, mouseY)) {
            RenderUtil.renderSlotHover(graphics, rect);
        }
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderSlot(graphics, mouseX, mouseY);
    }
}
