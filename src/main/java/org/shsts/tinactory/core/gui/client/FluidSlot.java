package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket1;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket1;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends MenuWidget {
    public static final int HIGHLIGHT_COLOR = 0x80FFFFFF;

    private final int tank;
    private final int syncSlot;

    public FluidSlot(IMenu menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
    }

    public FluidSlot(Menu<?, ?> menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
    }

    public FluidStack getFluidStack() {
        if (menu1 == null) {
            return menu.getSyncPacket(syncSlot, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
        } else {
            return menu1.getSyncPacket(syncSlot, FluidSyncPacket1.class)
                .map(FluidSyncPacket1::getFluidStack).orElse(FluidStack.EMPTY);
        }
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
        return Optional.of(ClientUtil.fluidTooltip(stack, true));
    }

    @Override
    protected boolean canClick(int button) {
        var menu2 = menu1 == null ? menu.getMenu() : menu1;
        return (button == 0 || button == 1) && !menu2.getCarried().isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        if (menu1 == null) {
            menu.triggerEvent(FLUID_SLOT_CLICK, () -> new SlotEventPacket(tank, button));
        } else {
            menu1.triggerEvent(MenuEventHandler.FLUID_SLOT_CLICK, (containerId, eventId) ->
                new SlotEventPacket1(containerId, eventId, tank, button));
        }
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
