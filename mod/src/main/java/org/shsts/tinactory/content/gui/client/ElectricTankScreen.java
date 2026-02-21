package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.gui.ElectricTankMenu;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.ElectricTankMenu.FILTER_SYNC;
import static org.shsts.tinactory.core.gui.LayoutMenu.FLUID_SYNC;
import static org.shsts.tinactory.core.gui.Texture.FLUID_SLOT_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankScreen extends ElectricStorageScreen<ElectricTankMenu> {
    private static class TankSlot extends FluidSlot {
        private final String filterName;

        public TankSlot(MenuBase menu, int index) {
            super(menu, index, FLUID_SYNC + index);
            this.filterName = FILTER_SYNC + index;
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

        for (var slot : layout.slots) {
            var rectBg = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
            var rect = rectBg.offset(1, 1).enlarge(-2, -2);
            layoutBg.addWidget(rectBg, new StaticWidget(menu, FLUID_SLOT_BG));
            layoutPanel.addWidget(rect, new TankSlot(menu, slot.index()));
        }
    }
}
