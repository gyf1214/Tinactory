package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankMenu extends Menu<BlockEntity, ElectricTankMenu> {
    private final Layout layout;
    private final Map<Layout.SlotInfo, Integer> fluidSyncIndex = new HashMap<>();
    private final Map<Layout.SlotInfo, Integer> filterSyncIndex = new HashMap<>();
    private final ElectricTank tank;

    public ElectricTankMenu(SmartMenuType<? extends BlockEntity, ?> type, int id,
        Inventory inventory, BlockEntity blockEntity, Layout layout) {
        super(type, id, inventory, blockEntity);
        this.layout = layout;
        this.tank = (ElectricTank) AllCapabilities.PROCESSOR.get(blockEntity);

        for (var slot : layout.slots) {
            fluidSyncIndex.put(slot, addFluidSlot(slot.index()));
            filterSyncIndex.put(slot, addSyncSlot(FluidSyncPacket::new,
                $ -> tank.getFilter(slot.index())));
        }
        this.height = layout.rect.endY();
    }

    @Override
    public void clickFluidSlot(int tankIndex, int button) {
        if (fluidContainer == null) {
            return;
        }
        if (fluidContainer.getFluidInTank(tankIndex).isEmpty()) {
            var filter = tank.getFilter(tankIndex);
            var carried = getCarried();
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
        super.clickFluidSlot(tankIndex, button);
    }

    @OnlyIn(Dist.CLIENT)
    private class TankSlot extends FluidSlot {
        private final int filterIndex;

        public TankSlot(Menu<?, ?> menu, Layout.SlotInfo slot) {
            super(menu, slot.index(), fluidSyncIndex.get(slot));
            this.filterIndex = filterSyncIndex.get(slot);
        }

        private FluidStack getFilterFluid() {
            return getSyncPacket(filterIndex, FluidSyncPacket.class)
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen1<ElectricTankMenu> createScreen(Inventory inventory, Component title) {
        var screen = new MenuScreen1<>(this, inventory, title);

        var layoutPanel = new Panel(screen);
        for (var slot : layout.slots) {
            var rect = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            layoutPanel.addWidget(rect, new StaticWidget(this, Texture.SLOT_BACKGROUND));
            layoutPanel.addWidget(rect1, new TankSlot(this, slot));
        }
        screen.addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);

        return screen;
    }

    public static <T extends BlockEntity> Menu.Factory<T, ElectricTankMenu> factory(Layout layout) {
        return (type, id, inventory, be) -> new ElectricTankMenu(type, id, inventory, be, layout);
    }
}
