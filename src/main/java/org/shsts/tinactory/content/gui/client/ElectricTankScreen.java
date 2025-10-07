package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.gui.ElectricTankMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.ElectricTankMenu.FILTER_SLOT;
import static org.shsts.tinactory.content.gui.ElectricTankMenu.FLUID_SLOT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankScreen extends ElectricStorageScreen<ElectricTankMenu> {
    private static class TankSlot extends FluidSlot {
        private final String filterName;

        public TankSlot(MenuBase menu, int index) {
            super(menu, index, FLUID_SLOT + index);
            this.filterName = FILTER_SLOT + index;
        }

        private FluidStack getFilterFluid() {
            return menu.getSyncPacket(filterName, FluidSyncPacket.class)
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
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return button == 0 || button == 1;
        }

        @Override
        protected void renderSlot(PoseStack poseStack, int mouseX, int mouseY) {
            var fluid = getFluidStack();
            var filter = getFilterFluid();
            if (fluid.isEmpty() && !filter.isEmpty()) {
                RenderUtil.renderGhostFluid(poseStack, filter, rect, getBlitOffset());
            }
            super.renderSlot(poseStack, mouseX, mouseY);
        }
    }

    public ElectricTankScreen(ElectricTankMenu menu, Component title) {
        super(menu, title);

        var layoutPanel = new Panel(this);
        var layout = menu.layout();
        for (var slot : layout.slots) {
            var rect = new Rect(slot.x() + 1, slot.y() + 1, SLOT_SIZE - 2, SLOT_SIZE - 2);
            layoutPanel.addWidget(rect, new TankSlot(menu, slot.index()));
        }
        addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
    }
}
