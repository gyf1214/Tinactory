package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.IMenuPlugin;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class InventoryPlugin<S extends MenuScreen> implements IMenuPlugin<S> {
    protected final IMenu menu;
    private final int endY;

    public InventoryPlugin(IMenu menu, int y) {
        this.menu = menu;
        var inventory = menu.inventory();
        var barY = y + 3 * SLOT_SIZE + SPACING;
        var barY1 = barY + MARGIN_TOP;
        for (var j = 0; j < 9; j++) {
            var x = MARGIN_HORIZONTAL + j * SLOT_SIZE;
            menu.addSlot(new Slot(inventory, j, x + 1, barY1 + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_HORIZONTAL + j * SLOT_SIZE;
                var y1 = y + i * SLOT_SIZE + MARGIN_TOP;
                menu.addSlot(new Slot(inventory, 9 + i * 9 + j, x + 1, y1 + 1));
            }
        }
        this.endY = barY + SLOT_SIZE;
    }

    protected enum FluidClickAction {
        NONE, FILL, DRAIN
    }

    protected record FluidClickResult(FluidClickAction action, ItemStack stack) {
        public FluidClickResult() {
            this(FluidClickAction.NONE, ItemStack.EMPTY);
        }
    }

    protected FluidClickResult doClickFluidSlot(ItemStack item, IFluidTank tank,
        boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(item);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        if (mayFill) {
            var capacity = tank.getCapacity() - tank.getFluidAmount();
            var fluid1 = handler.drain(capacity, IFluidHandler.FluidAction.SIMULATE);
            if (!fluid1.isEmpty()) {
                int amount = tank.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
                if (amount > 0) {
                    var fluid2 = new FluidStack(fluid1, amount);
                    var fluid3 = handler.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                    return new FluidClickResult(FluidClickAction.FILL,
                        handler.getContainer());
                }
            }
        }
        if (mayDrain && tank.getFluidAmount() > 0) {
            var fluid1 = tank.drain(tank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
            int amount = handler.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid2 = new FluidStack(fluid1, amount);
                var fluid3 = tank.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                handler.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                return new FluidClickResult(FluidClickAction.DRAIN,
                    handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    protected void clickFluidSlot(IFluidStackHandler container, int tankIndex, int button) {
        var tank = container.getTank(tankIndex);
        var item = menu.getMenu().getCarried();
        var outputItem = ItemStack.EMPTY;
        var mayDrain = true;
        var mayFill = true;
        while (!item.isEmpty()) {
            var item1 = ItemHandlerHelper.copyStackWithSize(item, 1);
            var clickResult = doClickFluidSlot(item1, tank, mayDrain, mayFill);
            if (clickResult.action == FluidClickAction.NONE) {
                break;
            } else if (clickResult.action == FluidClickAction.FILL) {
                mayDrain = false;
            } else {
                mayFill = false;
            }
            item.shrink(1);
            var retItem = clickResult.stack;
            var combinedItem = StackHelper.combineStack(outputItem, retItem);
            if (combinedItem.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(menu.player(), retItem);
            } else {
                outputItem = combinedItem.get();
            }
            if (button != 0) {
                break;
            }
        }
        if (item.isEmpty()) {
            menu.getMenu().setCarried(outputItem);
        } else {
            ItemHandlerHelper.giveItemToPlayer(menu.player(), outputItem);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public abstract Class<S> menuScreenClass();

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(S screen) {
        if (screen.contentHeight < endY) {
            screen.contentHeight = endY;
        }
    }
}
