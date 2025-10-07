package org.shsts.tinactory.core.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.slf4j.Logger;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InventoryMenu extends MenuBase {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int INVENTORY_HEIGHT = SLOT_SIZE * 4 + SPACING * 2;

    protected final int beginInvSlot;
    protected final int endInvSlot;
    protected final int endY;

    public InventoryMenu(Properties properties, int beginX, int beginY) {
        super(properties);
        this.beginInvSlot = slots.size();
        var beginY1 = beginY + SPACING + MARGIN_TOP;
        var barY = beginY + 3 * SLOT_SIZE + SPACING * 2;
        var barY1 = barY + MARGIN_TOP;
        for (var j = 0; j < 9; j++) {
            var x = MARGIN_X + beginX + j * SLOT_SIZE;
            addSlot(new Slot(inventory, j, x + 1, barY1 + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_X + beginX + j * SLOT_SIZE;
                var y1 = beginY1 + i * SLOT_SIZE;
                addSlot(new Slot(inventory, 9 + i * 9 + j, x + 1, y1 + 1));
            }
        }
        this.endInvSlot = slots.size();
        this.endY = barY + SLOT_SIZE;
    }

    public InventoryMenu(Properties properties, int beginY) {
        this(properties, 0, beginY);
    }

    public int endY() {
        return endY;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = getSlot(index);
        return quickMoveStack(slot) ? slot.getItem() : ItemStack.EMPTY;
    }

    protected boolean quickMoveStack(Slot slot) {
        if (world.isClientSide) {
            return false;
        }
        if (!slot.hasItem()) {
            return false;
        }
        var inv = new PlayerMainInvWrapper(inventory);
        if (slot.index < beginInvSlot || slot.index >= endInvSlot) {
            if (!slot.mayPickup(player)) {
                return false;
            }
            var oldStack = slot.getItem().copy();
            var stack = oldStack.copy();
            var reminder = ItemHandlerHelper.insertItemStacked(inv, stack, true);
            stack.shrink(reminder.getCount());
            if (stack.isEmpty()) {
                return false;
            }
            var stack1 = slot.safeTake(stack.getCount(), Integer.MAX_VALUE, player);
            ItemHandlerHelper.insertItemStacked(inv, stack1, false);

            return ItemStack.isSame(oldStack, slot.getItem());
        } else {
            var invIndex = slot.getContainerSlot();
            var oldStack = inv.getStackInSlot(invIndex).copy();
            var stack = oldStack.copy();
            var amount = stack.getCount();
            for (var i = beginInvSlot == 0 ? endInvSlot : 0; i < slots.size();
                i = i + 1 == beginInvSlot ? endInvSlot : i + 1) {
                var targetSlot = getSlot(i);
                if (!targetSlot.mayPlace(stack)) {
                    continue;
                }
                var reminder = targetSlot.safeInsert(stack);
                if (reminder.getCount() < amount) {
                    inv.extractItem(invIndex, amount - reminder.getCount(), false);
                    return ItemStack.isSame(oldStack, slot.getItem());
                }
            }
            return false;
        }
    }

    public enum FluidClickAction {
        NONE, FILL, DRAIN
    }

    public record FluidClickResult(FluidClickAction action, ItemStack stack) {
        public FluidClickResult() {
            this(FluidClickAction.NONE, ItemStack.EMPTY);
        }
    }

    @FunctionalInterface
    protected interface IFluidClickExecutor {
        FluidClickResult click(ItemStack carried, boolean mayDrain, boolean mayFill);
    }

    private FluidClickResult doClickFluidSlot(ItemStack carried, IFluidTank tank,
        boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(carried);
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
                    var fluid2 = StackHelper.copyWithAmount(fluid1, amount);
                    var fluid3 = handler.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                    var amount1 = tank.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                    if (amount1 != amount) {
                        LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount1, amount);
                    }
                    return new FluidClickResult(FluidClickAction.FILL,
                        handler.getContainer());
                }
            }
        }
        if (mayDrain && tank.getFluidAmount() > 0) {
            var fluid1 = tank.drain(tank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
            int amount = handler.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid2 = StackHelper.copyWithAmount(fluid1, amount);
                var fluid3 = tank.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                var amount1 = handler.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                if (amount1 != amount) {
                    LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount1, amount);
                }
                return new FluidClickResult(FluidClickAction.DRAIN,
                    handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    protected boolean clickFluidSlot(IFluidClickExecutor clickExecutor, int button) {
        var item = getCarried();
        var outputItem = ItemStack.EMPTY;
        var mayDrain = true;
        var mayFill = true;
        var success = false;
        while (!item.isEmpty()) {
            var item1 = StackHelper.copyWithCount(item, 1);
            var clickResult = clickExecutor.click(item1, mayDrain, mayFill);
            if (clickResult.action == FluidClickAction.NONE) {
                break;
            } else if (clickResult.action == FluidClickAction.FILL) {
                mayDrain = false;
            } else {
                mayFill = false;
            }
            success = true;
            item.shrink(1);
            var retItem = clickResult.stack;
            var combinedItem = StackHelper.combineStack(outputItem, retItem);
            if (combinedItem.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, retItem);
            } else {
                outputItem = combinedItem.get();
            }
            if (button != 0) {
                break;
            }
        }
        if (item.isEmpty()) {
            setCarried(outputItem);
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, outputItem);
        }
        return success;
    }

    protected void clickFluidSlot(IFluidStackHandler container, int tankIndex, int button) {
        var tank = container.getTank(tankIndex);
        clickFluidSlot((carried, mayDrain, mayFill) ->
            doClickFluidSlot(carried, tank, mayDrain, mayFill), button);
    }
}
