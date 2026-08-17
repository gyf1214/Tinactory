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
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.content.logistics.MEStorageInterface;
import org.shsts.tinactory.integration.gui.InventoryMenu;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.slf4j.Logger;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.AllMenus.STORAGE_SYNC;
import static org.shsts.tinactory.content.gui.sync.StorageEventPacket.QUICK_MOVE_BUTTON;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceMenu extends InventoryMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_SYNC = "slots";
    public static final int PANEL_HEIGHT = 7 * SLOT_SIZE + SPACING;

    private final IMachine machine;
    private final MEStorageInterface storageInterface;
    private final Runnable updateListener;

    public MEStorageInterfaceMenu(Properties properties) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity());
        this.storageInterface = getContainer(blockEntity(), MEStorageInterface.ID, MEStorageInterface.class);

        var scheduler = new ActiveScheduler<>(STORAGE_SYNC, () -> new StorageSyncPacket(
            storageInterface.getAllItems(), storageInterface.getAllFluids()));
        this.updateListener = scheduler::invokeUpdate;

        addSyncSlot(SLOT_SYNC, scheduler);
        if (!world.isClientSide) {
            storageInterface.onUpdate(updateListener);
        }

        onEventPacket(STORAGE_SLOT, this::onSlotClick);
        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(player);
        if (!world.isClientSide) {
            storageInterface.unregisterListener(updateListener);
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
        var fluidPort = storageInterface.fluidPort();

        if (packet.isItem() && packet.button() == QUICK_MOVE_BUTTON) {
            quickMoveStack(packet.key());
            return;
        }

        boolean success;
        if (packet.isFluid()) {
            var key = packet.key();
            success = clickFluidSlot((carried, mayDrain, mayFill) ->
                doClickFluidSlot(carried, fluidPort, key, mayDrain, mayFill), button);
        } else if (button == 1) {
            success = clickFluidSlot((carried, mayDrain, mayFill) ->
                doClickEmptyFluidSlot(carried, fluidPort, mayFill), button);
        } else {
            success = false;
        }
        if (!success) {
            var itemPort = storageInterface.itemPort();
            clickItemSlot(getCarried(), packet.isItem() ? packet.key() : null, itemPort, button);
        }
    }

    private void quickMoveStack(IStackKey key) {
        var inv = new PlayerMainInvWrapper(inventory);
        var target = storageInterface.itemPort();
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
        var target = storageInterface.itemPort();
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
}
