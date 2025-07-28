package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidSlot extends MenuWidget {
    public static final int HIGHLIGHT_COLOR = 0x80FFFFFF;

    private final int tank;
    private final int syncSlot;
    @Nullable
    private final String syncName;

    public FluidSlot(IMenu menu, int tank, int syncSlot) {
        super(menu);
        this.tank = tank;
        this.syncSlot = syncSlot;
        this.syncName = null;
    }

    public FluidSlot(IMenu menu, int tank, String syncName) {
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
        return (button == 0 || button == 1) && !menu.getMenu().getCarried().isEmpty();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        menu.triggerEvent(FLUID_SLOT_CLICK, () -> new SlotEventPacket(tank, button));
    }

    protected void renderBackground(PoseStack poseStack) {
        RenderUtil.blit(poseStack, SLOT_BACKGROUND, getBlitOffset(), rect.offset(-1, -1).enlarge(2, 2));
    }

    protected void renderSlot(PoseStack poseStack, int mouseX, int mouseY) {
        RenderUtil.renderFluidWithDecoration(poseStack, getFluidStack(), rect, getBlitOffset());
        if (isHovering(mouseX, mouseY)) {
            RenderSystem.colorMask(true, true, true, false);
            RenderUtil.fill(poseStack, rect, HIGHLIGHT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        renderSlot(poseStack, mouseX, mouseY);
    }
}
