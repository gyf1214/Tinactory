package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankPlugin extends ElectricStoragePlugin {
    private final Map<Layout.SlotInfo, Integer> fluidSyncIndex = new HashMap<>();
    private final Map<Layout.SlotInfo, Integer> filterSyncIndex = new HashMap<>();
    private final ElectricTank tank;
    private final IFluidStackHandler container;

    public ElectricTankPlugin(IMenu menu) {
        super(menu);
        this.tank = EVENT_MANAGER.get(menu.blockEntity())
            .getProvider(modLoc(ElectricTank.ID), ElectricTank.class);
        this.container = FLUID_STACK_HANDLER.get(menu.blockEntity());

        for (var slot : layout.slots) {
            fluidSyncIndex.put(slot, menu.addSyncSlot($ ->
                new FluidSyncPacket(container.getFluidInTank(slot.index()))));
            filterSyncIndex.put(slot, menu.addSyncSlot($ ->
                new FluidSyncPacket(tank.getFilter(slot.index()))));
        }

        menu.onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(p.getIndex(), p.getButton()));
    }

    private void clickFluidSlot(int tankIndex, int button) {
        if (container.getFluidInTank(tankIndex).isEmpty()) {
            var filter = tank.getFilter(tankIndex);
            var carried = menu.getMenu().getCarried();
            var fluidCarried = StackHelper.getFluidHandlerFromItem(carried)
                .map(h -> h.getFluidInTank(0))
                .orElse(FluidStack.EMPTY);
            if (carried.isEmpty() && !filter.isEmpty()) {
                tank.resetFilter(tankIndex);
                return;
            } else if (!fluidCarried.isEmpty() && !filter.isFluidEqual(fluidCarried)) {
                tank.setFilter(tankIndex, fluidCarried);
                if (!filter.isEmpty() || !tank.isUnlocked()) {
                    return;
                }
            }
        }
        clickFluidSlot(container, tankIndex, button);
    }

    @OnlyIn(Dist.CLIENT)
    private class TankSlot extends FluidSlot {
        private final int filterIndex;

        public TankSlot(IMenu menu, Layout.SlotInfo slot) {
            super(menu, slot.index(), fluidSyncIndex.get(slot));
            this.filterIndex = filterSyncIndex.get(slot);
        }

        private FluidStack getFilterFluid() {
            return menu.getSyncPacket(filterIndex, FluidSyncPacket.class)
                .map(FluidSyncPacket::getFluidStack).orElse(FluidStack.EMPTY);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var fluid = getFluidStack();
            var filter = getFilterFluid();
            if (fluid.isEmpty() && !filter.isEmpty()) {
                return Optional.of(ClientUtil.fluidTooltip(filter, false));
            }
            return super.getTooltip(mouseX, mouseY);
        }

        @Override
        protected boolean canClick(int button) {
            return button == 0 || button == 1;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var fluid = getFluidStack();
            var filter = getFilterFluid();
            if (fluid.isEmpty() && !filter.isEmpty()) {
                RenderUtil.renderGhostFluid(poseStack, filter, rect, getBlitOffset());
            }

            super.doRender(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(MenuScreen screen) {
        super.applyMenuScreen(screen);

        var layoutPanel = new Panel(screen);
        for (var slot : layout.slots) {
            var rect = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            layoutPanel.addWidget(rect, new StaticWidget(menu, SLOT_BACKGROUND));
            layoutPanel.addWidget(rect1, new TankSlot(menu, slot));
        }
        screen.addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
    }
}
