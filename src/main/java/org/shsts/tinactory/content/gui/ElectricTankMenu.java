package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.StackHelper;

import static org.shsts.tinactory.content.AllCapabilities.EVENT_MANAGER;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricTankMenu extends ElectricStorageMenu {
    public static final String FLUID_SLOT = ProcessingMenu.FLUID_SLOT;
    public static final String FILTER_SLOT = "filterSlot_";

    private final ElectricTank tank;
    private final IFluidStackHandler fluidHandler;

    public ElectricTankMenu(Properties properties) {
        super(properties);
        this.tank = EVENT_MANAGER.get(blockEntity)
            .getProvider(modLoc(ElectricTank.ID), ElectricTank.class);
        this.fluidHandler = FLUID_STACK_HANDLER.get(blockEntity);

        for (var slot : layout.slots) {
            addSyncSlot(FLUID_SLOT + slot.index(), () ->
                new FluidSyncPacket(fluidHandler.getFluidInTank(slot.index())));
            addSyncSlot(FILTER_SLOT + slot.index(), () ->
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
