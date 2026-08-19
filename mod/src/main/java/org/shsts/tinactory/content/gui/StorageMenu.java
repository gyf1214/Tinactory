package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.gui.InventoryMenu;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.AllMenus.STORAGE_SYNC;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class StorageMenu extends InventoryMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_SYNC = "slots";
    public static final int PANEL_HEIGHT = 7 * SLOT_SIZE + SPACING;

    private final IMachine machine;
    private final IPort<ItemStack> itemPort;
    private final IPort<FluidStack> fluidPort;
    private final int itemStackLimit;
    private final int fluidStackLimit;
    private final Runnable updateListener;

    protected StorageMenu(Properties properties, IPort<ItemStack> itemPort, int itemStackLimit,
        IPort<FluidStack> fluidPort, int fluidStackLimit) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity());
        this.itemPort = itemPort;
        this.itemStackLimit = itemStackLimit;
        this.fluidPort = fluidPort;
        this.fluidStackLimit = fluidStackLimit;

        var scheduler = new ActiveScheduler<>(STORAGE_SYNC, this::storageEntries);
        this.updateListener = scheduler::invokeUpdate;

        addSyncSlot(SLOT_SYNC, scheduler);
        if (!world.isClientSide) {
            if (itemPort instanceof IPortNotifier notifier) {
                notifier.onUpdate(updateListener);
            }
            if (fluidPort instanceof IPortNotifier notifier && fluidPort != (Object) itemPort) {
                notifier.onUpdate(updateListener);
            }
        }

        onEventPacket(STORAGE_SLOT, this::onSlotClick);
        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!world.isClientSide) {
            if (itemPort instanceof IPortNotifier notifier) {
                notifier.unregisterListener(updateListener);
            }
            if (fluidPort instanceof IPortNotifier notifier && fluidPort != (Object) itemPort) {
                notifier.unregisterListener(updateListener);
            }
        }
    }

    private FluidClickResult doClickFluidSlot(ItemStack carried, IPort<FluidStack> port,
        IStackKey key, boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        var fluid1 = StackHelper.FLUID_ADAPTER.stackOf(key, Integer.MAX_VALUE);
        if (mayFill) {
            var fluid2 = handler.drain(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (StackHelper.transmitFluidFromHandler(handler, port, fluid2)) {
                return new FluidClickResult(FluidClickAction.FILL, handler.getContainer());
            }
        }
        if (mayDrain) {
            var fluid2 = port.extract(fluid1, true);
            int amount = handler.fill(fluid2, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid3 = StackHelper.copyWithAmount(fluid2, amount);
                var fluid4 = port.extract(fluid3, false);
                var amount1 = handler.fill(fluid4, IFluidHandler.FluidAction.EXECUTE);
                if (amount1 != amount) {
                    LOGGER.warn("Failed to execute fluid drain extracted={}/{}", amount1, amount);
                }
                return new FluidClickResult(FluidClickAction.DRAIN, handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    private FluidClickResult doClickEmptyFluidSlot(ItemStack carried, IPort<FluidStack> port, boolean mayFill) {
        if (!mayFill) {
            return new FluidClickResult();
        }
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        var fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (StackHelper.transmitFluidFromHandler(handler, port, fluid)) {
            return new FluidClickResult(FluidClickAction.FILL, handler.getContainer());
        }
        return new FluidClickResult();
    }

    private void clickItemSlot(ItemStack carried, @Nullable IStackKey key, IPort<ItemStack> port, int button) {
        if (!carried.isEmpty()) {
            if (button == 1) {
                var carried1 = StackHelper.copyWithCount(carried, 1);
                carried.shrink(1);
                var remaining = port.insert(carried1, false);
                var combined = StackHelper.combineStack(carried, remaining);
                if (combined.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, remaining);
                } else {
                    setCarried(combined.get());
                }
            } else {
                setCarried(port.insert(carried, false));
            }
        } else if (key != null) {
            var item = StackHelper.ITEM_ADAPTER.stackOf(key);
            var count = Math.min((int) port.getStorageAmount(item), item.getMaxStackSize());
            var count1 = button == 1 ? (count + 1) / 2 : count;
            var item1 = StackHelper.copyWithCount(item, count1);
            var extracted = port.extract(item1, false);
            setCarried(extracted);
        }
    }

    private void onSlotClick(StorageEventPacket packet) {
        var button = packet.button();
        if (packet.isItem() && packet.isQuickMove()) {
            quickMoveStack(packet.key());
            return;
        }

        var carried = getCarried();
        var carriedFluid = StackHelper.getFluidFromItem(carried);
        IStackKey carriedKey = carried.isEmpty() ? null :
            (carriedFluid.isEmpty() ? StackHelper.ITEM_ADAPTER.keyOf(carried) :
                StackHelper.FLUID_ADAPTER.keyOf(carriedFluid));
        if (!packet.isEmpty() && filters().contains(packet.key()) && storageAmount(packet.key()) == 0) {
            if (carriedKey == null) {
                resetFilter(packet.key());
                return;
            } else if (!carriedKey.equals(packet.key())) {
                resetFilter(packet.key());
                setFilter(carriedKey);
                return;
            }
        } else if (packet.isEmpty() && !isUnlocked() && carriedKey != null) {
            setFilter(carriedKey);
            return;
        }

        boolean success;
        if (packet.isFluid()) {
            var key = packet.key();
            success = clickFluidSlot((carried1, mayDrain, mayFill) ->
                doClickFluidSlot(carried1, fluidPort, key, mayDrain, mayFill), button);
        } else if (button == 1) {
            success = clickFluidSlot((carried1, mayDrain, mayFill) ->
                doClickEmptyFluidSlot(carried1, fluidPort, mayFill), button);
        } else {
            success = false;
        }
        if (!success) {
            clickItemSlot(getCarried(), packet.isItem() ? packet.key() : null, itemPort, button);
        }
    }

    private long storageAmount(IStackKey key) {
        return switch (key.type()) {
            case ITEM -> itemPort.getStorageAmount(StackHelper.ITEM_ADAPTER.stackOf(key));
            case FLUID -> fluidPort.getStorageAmount(StackHelper.FLUID_ADAPTER.stackOf(key));
            case NONE -> 0;
        };
    }

    private void quickMoveStack(IStackKey key) {
        var inv = new PlayerMainInvWrapper(inventory);
        var target = itemPort;
        var stack = StackHelper.ITEM_ADAPTER.stackOf(key, Integer.MAX_VALUE);
        var extracted = target.extract(stack, true);
        var remaining = ItemHandlerHelper.insertItemStacked(inv, extracted, true);
        var inserted = extracted.getCount() - remaining.getCount();
        if (inserted <= 0) {
            return;
        }
        var extracted1 = StackHelper.copyWithCount(extracted, inserted);
        var extracted2 = target.extract(extracted1, false);
        var remaining1 = ItemHandlerHelper.insertItemStacked(inv, extracted2, false);
        if (!remaining1.isEmpty()) {
            LOGGER.warn("{}: Failed to quick move inventory, extracted {}/{}", blockEntity,
                extracted2.getCount() - remaining1.getCount(), extracted2.getCount());
        }
    }

    /**
     * This only handles quick move clicking on vanilla slots, i.e. inventory.
     * <p>
     * Only deals with item for now.
     */
    @Override
    protected boolean quickMoveStack(Slot slot) {
        if (world.isClientSide) {
            return false;
        }
        if (!slot.hasItem()) {
            return false;
        }
        var inv = new PlayerMainInvWrapper(inventory);
        assert slot.index >= beginInvSlot && slot.index < endInvSlot;

        var index = slot.getContainerSlot();
        var stack = inv.getStackInSlot(index);
        var target = itemPort;
        if (!target.acceptInput(stack)) {
            return false;
        }
        var remaining = target.insert(stack, true);
        var inserted = stack.getCount() - remaining.getCount();
        if (inserted <= 0) {
            return false;
        }
        var stack1 = inv.extractItem(index, inserted, false);
        var remaining1 = target.insert(stack1, false);
        if (!remaining1.isEmpty()) {
            LOGGER.warn("{}: Failed to quick move inventory, inserted {}/{}", blockEntity,
                stack1.getCount() - remaining1.getCount(), stack1.getCount());
        }
        return false;
    }

    protected Collection<IStackKey> filters() {
        return List.of();
    }

    protected boolean isUnlocked() {
        return true;
    }

    protected boolean setFilter(IStackKey key) {
        return false;
    }

    protected boolean resetFilter(IStackKey key) {
        return false;
    }

    private StorageSyncPacket storageEntries() {
        var amounts = new HashMap<IStackKey, Long>();
        itemPort.getAllStorages().forEach(stack -> amounts.put(StackHelper.ITEM_ADAPTER.keyOf(stack),
            (long) stack.getCount()));
        fluidPort.getAllStorages().forEach(stack -> amounts.put(StackHelper.FLUID_ADAPTER.keyOf(stack),
            (long) stack.getAmount()));
        var entries = new ArrayList<StorageEntry>();
        for (var key : filters()) {
            addEntries(entries, key, amounts.remove(key), true);
        }
        amounts.forEach((key, amount) -> addEntries(entries, key, amount, false));
        return new StorageSyncPacket(entries);
    }

    private void addEntries(Collection<StorageEntry> entries, IStackKey key, Long amount, boolean isFilter) {
        var remaining = amount == null ? 0L : amount;
        var limit = key.type() == PortType.ITEM ? itemStackLimit : fluidStackLimit;
        if (limit == 0) {
            entries.add(new StorageEntry(key, remaining, isFilter));
            return;
        }
        do {
            var entryAmount = Math.min(remaining, limit);
            entries.add(new StorageEntry(key, entryAmount, isFilter));
            remaining -= entryAmount;
            isFilter = false;
        } while (remaining > 0);
    }
}
