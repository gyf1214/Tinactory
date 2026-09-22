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
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.FilterEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.gui.InventoryMenu;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.FILTER_SLOT;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.AllMenus.STORAGE_SYNC;
import static org.shsts.tinactory.AllNetworks.STORAGE_FILTERS;
import static org.shsts.tinactory.core.gui.Menu.SEARCH_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageMenu extends InventoryMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_SYNC = "slots";
    public static final int PANEL_HEIGHT = SEARCH_SIZE + 7 * SLOT_SIZE + SPACING * 2;

    private final IMachine machine;
    private final IPort<ItemStack> itemPort;
    private final IPort<FluidStack> fluidPort;
    private final int itemStackLimit;
    private final int fluidStackLimit;
    private final int storageSlots;
    private final int filterSlots;
    private final Runnable updateListener;

    public StorageMenu(Properties properties, IPort<ItemStack> itemPort, int itemStackLimit,
        IPort<FluidStack> fluidPort, int fluidStackLimit, int storageSlots, int filterSlots) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity());
        this.itemPort = itemPort;
        this.itemStackLimit = itemStackLimit;
        this.fluidPort = fluidPort;
        this.fluidStackLimit = fluidStackLimit;
        this.storageSlots = storageSlots;
        this.filterSlots = filterSlots;

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
        onEventPacket(FILTER_SLOT, this::onFilterClick);
        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    public StorageMenu(Properties properties, IPort<ItemStack> itemPort, int itemStackLimit,
        IPort<FluidStack> fluidPort, int fluidStackLimit) {
        this(properties, itemPort, itemStackLimit, fluidPort, fluidStackLimit,
            Integer.MAX_VALUE, 0);
    }

    public int storageSlots() {
        return storageSlots;
    }

    public int filterSlots() {
        return filterSlots;
    }

    public boolean allowItemFilter() {
        return filterSlots > 0 && itemPort.type() != PortType.NONE;
    }

    public boolean allowFluidFilter() {
        return filterSlots > 0 && fluidPort.type() != PortType.NONE;
    }

    public boolean allowTagFilter() {
        return allowItemFilter();
    }

    public IMachineConfig machineConfig() {
        return machine.config();
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
        IStackKey key, int maxDrain, boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        var fluid = StackHelper.FLUID_ADAPTER.stackOf(key, Integer.MAX_VALUE);
        if (mayFill) {
            var fluid2 = handler.drain(fluid, IFluidHandler.FluidAction.SIMULATE);
            if (StackHelper.transmitFluidFromHandler(handler, port, fluid2)) {
                return new FluidClickResult(FluidClickAction.FILL, handler.getContainer(), 0);
            }
        }
        if (mayDrain) {
            var fluid1 = StackHelper.FLUID_ADAPTER.stackOf(key, maxDrain);
            var fluid2 = port.extract(fluid1, true);
            int amount = handler.fill(fluid2, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid3 = StackHelper.copyWithAmount(fluid2, amount);
                var fluid4 = port.extract(fluid3, false);
                var amount1 = handler.fill(fluid4, IFluidHandler.FluidAction.EXECUTE);
                if (amount1 != amount) {
                    LOGGER.warn("Failed to execute fluid drain extracted={}/{}", amount1, amount);
                }
                return new FluidClickResult(FluidClickAction.DRAIN, handler.getContainer(), amount1);
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
            return new FluidClickResult(FluidClickAction.FILL, handler.getContainer(), 0);
        }
        return new FluidClickResult();
    }

    private void clickItemSlot(ItemStack carried, @Nullable IStackKey key,
        IPort<ItemStack> port, long amount, int button) {
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
            var count = (int) Math.min(port.getStorageAmount(item), Math.min(amount, item.getMaxStackSize()));
            var count1 = button == 1 ? (count + 1) / 2 : count;
            var item1 = StackHelper.copyWithCount(item, count1);
            var extracted = port.extract(item1, false);
            setCarried(extracted);
        }
    }

    private void onSlotClick(StorageEventPacket packet) {
        var button = packet.button();
        var carried = getCarried();

        if (carried.isEmpty() && packet.isItem() && packet.shiftPressed()) {
            quickMoveStack(packet.key());
            return;
        }

        var handler = StackHelper.getFluidHandlerFromItem(StackHelper.copyWithCount(carried, 1));
        var hasDrainableFluid = handler.isPresent() &&
            !handler.get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE).isEmpty();
        var fluidClick = handler.isPresent() && (packet.isFluid() || hasDrainableFluid);
        if (fluidClick && !packet.shiftPressed()) {
            if (clickFluidEntry(packet, button) || packet.isFluid() ||
                hasDrainableFluid && fluidPort.type() != PortType.NONE) {
                return;
            }
        }
        clickItemSlot(carried, packet.isItem() ? packet.key() : null, itemPort, packet.amount(), button);
    }

    private boolean clickFluidEntry(StorageEventPacket packet, int button) {
        var maxDrain = fluidStackLimit > 0 ? (int) Math.min(packet.amount(), Integer.MAX_VALUE) : Integer.MAX_VALUE;
        if (packet.isFluid()) {
            var key = packet.key();
            return clickFluidSlot((carried1, maxDrain1, mayDrain, mayFill) ->
                doClickFluidSlot(carried1, fluidPort, key, maxDrain1, mayDrain, mayFill), maxDrain, button);
        } else {
            return clickFluidSlot((carried1, maxDrain1, mayDrain, mayFill) ->
                doClickEmptyFluidSlot(carried1, fluidPort, mayFill), maxDrain, button);
        }
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

    public FilterEntry getFilter(int index) {
        if (index < 0 || index >= filterSlots) {
            return FilterEntry.EMPTY;
        }
        return machineConfig().get(STORAGE_FILTERS)
            .filter(list -> index < list.size())
            .map(list -> list.get(index))
            .orElse(FilterEntry.EMPTY);
    }

    private void onFilterClick(FilterEventPacket packet) {
        var list = machineConfig().get(STORAGE_FILTERS)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
        var remove = packet.remove().orElse(-1);
        var append = packet.append();
        var hasRemove = remove >= 0 && remove < list.size();
        var hasAppend = append.type() != FilterEntry.Type.NONE;

        if (hasRemove && hasAppend) {
            list.set(remove, append);
        } else if (hasRemove) {
            list.remove(remove);
        } else if (hasAppend && list.size() < filterSlots) {
            list.add(append);
        } else {
            return;
        }

        machine.setConfig(SetMachineConfigPacket.builder()
            .set(STORAGE_FILTERS, list)
            .get());
    }

    private StorageSyncPacket storageEntries() {
        var entries = new ArrayList<StorageEntry>();
        itemPort.getAllStorages().forEach(stack -> addEntries(entries,
            StackHelper.ITEM_ADAPTER.keyOf(stack), stack.getCount()));
        fluidPort.getAllStorages().forEach(stack -> addEntries(entries,
            StackHelper.FLUID_ADAPTER.keyOf(stack), stack.getAmount()));
        return new StorageSyncPacket(entries);
    }

    private void addEntries(Collection<StorageEntry> entries, IStackKey key, long amount) {
        var remaining = amount;
        var limit = key.type() == PortType.ITEM ? itemStackLimit : fluidStackLimit;
        if (limit == 0) {
            entries.add(new StorageEntry(key, remaining));
            return;
        }
        do {
            var entryAmount = Math.min(remaining, limit);
            entries.add(new StorageEntry(key, entryAmount));
            remaining -= entryAmount;
        } while (remaining > 0);
    }
}
