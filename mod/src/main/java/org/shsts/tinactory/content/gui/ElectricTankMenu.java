package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.logistics.IFluidTanksHandler;
import org.shsts.tinactory.core.logistics.StackHelper;

import static org.shsts.tinactory.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankMenu extends ElectricStorageMenu {
    public static final String FILTER_SYNC = "filterSlot_";
    private final ElectricTank tank;
    private final IFluidTanksHandler fluidHandler;

    public ElectricTankMenu(Properties properties) {
        super(properties);
        this.tank = getProvider(blockEntity(), ElectricTank.ID, ElectricTank.class);
        this.fluidHandler = MENU_FLUID_HANDLER.get(blockEntity());

        for (var slot : layout.slots) {
            addSyncSlot(FLUID_SYNC + slot.index(), () ->
                new FluidSyncPacket(fluidHandler.getFluidInTank(slot.index())));
            addSyncSlot(FILTER_SYNC + slot.index(), () ->
                new FluidSyncPacket(tank.getFilter(slot.index())));
        }

        onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(p.getIndex(), p.getButton()));
    }

    private void clickFluidSlot(int tankIndex, int button) {
        if (fluidHandler.getFluidInTank(tankIndex).isEmpty()) {
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
        clickFluidSlot(fluidHandler, tankIndex, button);
    }
}
