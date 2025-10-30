package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.content.logistics.MEStorageInterface;
import org.shsts.tinactory.core.gui.InventoryMenu;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.slf4j.Logger;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.ME_STORAGE_INTERFACE_SLOT;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket.QUICK_MOVE_BUTTON;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceMenu extends InventoryMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_SYNC = "slots";
    public static final int PANEL_HEIGHT = 7 * SLOT_SIZE + SPACING;

    private final IMachine machine;
    private final IMachineConfig machineConfig;
    private final MEStorageInterface storageInterface;
    private final Runnable updateListener;

    public MEStorageInterfaceMenu(Properties properties) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity);
        this.machineConfig = machine.config();
        this.storageInterface = getProvider(blockEntity, MEStorageInterface.ID, MEStorageInterface.class);

        var scheduler = new ActiveScheduler<>(() -> new MEStorageInterfaceSyncPacket(
            storageInterface.getAllItems(), storageInterface.getAllFluids()));
        this.updateListener = scheduler::invokeUpdate;

        addSyncSlot(SLOT_SYNC, scheduler);
        if (!world.isClientSide) {
            storageInterface.onUpdate(updateListener);
        }

        onEventPacket(ME_STORAGE_INTERFACE_SLOT, this::onSlotClick);
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

    public IMachineConfig machineConfig() {
        return machineConfig;
    }

    private FluidClickResult doClickFluidSlot(ItemStack carried, IFluidCollection port,
        FluidStack fluid, boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        if (mayFill) {
            var fluid1 = StackHelper.copyWithAmount(fluid, Integer.MAX_VALUE);
            var fluid2 = handler.drain(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (!fluid2.isEmpty()) {
                int amount = port.fill(fluid2, true);
                if (amount > 0) {
                    var fluid3 = StackHelper.copyWithAmount(fluid2, amount);
                    var fluid4 = handler.drain(fluid3, IFluidHandler.FluidAction.EXECUTE);
                    var amount1 = port.fill(fluid4, false);
                    if (amount1 != amount) {
                        LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount1, amount);
                    }
                    return new FluidClickResult(FluidClickAction.FILL,
                        handler.getContainer());
                }
            }
        }
        if (mayDrain) {
            var fluid1 = port.drain(fluid, true);
            int amount = handler.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid2 = StackHelper.copyWithAmount(fluid1, amount);
                var fluid3 = port.drain(fluid2, false);
                var amount1 = handler.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                if (amount1 != amount) {
                    LOGGER.warn("Failed to execute fluid drain extracted={}/{}", amount1, amount);
                }
                return new FluidClickResult(FluidClickAction.DRAIN,
                    handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    private FluidClickResult doClickEmptyFluidSlot(ItemStack carried, IFluidCollection port, boolean mayFill) {
        if (!mayFill) {
            return new FluidClickResult();
        }
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        var fluid = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
        if (!fluid.isEmpty()) {
            int amount = port.fill(fluid, true);
            if (amount > 0) {
                var fluid1 = StackHelper.copyWithAmount(fluid, amount);
                var fluid2 = handler.drain(fluid1, IFluidHandler.FluidAction.EXECUTE);
                var amount1 = port.fill(fluid2, false);
                if (amount1 != amount) {
                    LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount1, amount);
                }
                return new FluidClickResult(FluidClickAction.FILL,
                    handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    private void clickItemSlot(ItemStack carried, ItemStack item, IItemCollection port, int button) {
        if (!carried.isEmpty()) {
            if (button == 1) {
                var carried1 = StackHelper.copyWithCount(carried, 1);
                carried.shrink(1);
                var remaining = port.insertItem(carried1, false);
                var combined = StackHelper.combineStack(carried, remaining);
                if (combined.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, remaining);
                } else {
                    setCarried(combined.get());
                }
            } else {
                setCarried(port.insertItem(carried, false));
            }
        } else {
            var count = Math.min(item.getCount(), item.getMaxStackSize());
            var count1 = button == 1 ? (count + 1) / 2 : count;
            var item1 = StackHelper.copyWithCount(item, count1);
            var extracted = port.extractItem(item1, false);
            setCarried(extracted);
        }
    }

    private void onSlotClick(MEStorageInterfaceEventPacket packet) {
        var button = packet.button();
        var fluidPort = storageInterface.fluidPort();

        if (packet.isItem() && packet.button() == QUICK_MOVE_BUTTON) {
            quickMoveStack(packet.item());
            return;
        }

        boolean success;
        if (packet.isFluid()) {
            var fluid = packet.fluid();
            success = clickFluidSlot((carried, mayDrain, mayFill) ->
                doClickFluidSlot(carried, fluidPort, fluid, mayDrain, mayFill), button);
        } else if (button == 1) {
            success = clickFluidSlot((carried, mayDrain, mayFill) ->
                doClickEmptyFluidSlot(carried, fluidPort, mayFill), button);
        } else {
            success = false;
        }
        if (!success) {
            var item = packet.isItem() ? packet.item() : ItemStack.EMPTY;
            var itemPort = storageInterface.itemPort();
            clickItemSlot(getCarried(), item, itemPort, button);
        }
    }

    private void quickMoveStack(ItemStack stack) {
        var inv = new PlayerMainInvWrapper(inventory);
        var target = storageInterface.itemPort();
        var extracted = target.extractItem(stack, true);
        var remaining = ItemHandlerHelper.insertItemStacked(inv, extracted, true);
        var inserted = extracted.getCount() - remaining.getCount();
        if (inserted <= 0) {
            return;
        }
        var extracted1 = StackHelper.copyWithCount(extracted, inserted);
        var extracted2 = target.extractItem(extracted1, false);
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
        var remaining = target.insertItem(stack, true);
        var inserted = stack.getCount() - remaining.getCount();
        if (inserted <= 0) {
            return false;
        }
        var stack1 = inv.extractItem(index, inserted, false);
        var remaining1 = target.insertItem(stack1, false);
        if (!remaining1.isEmpty()) {
            LOGGER.warn("{}: Failed to quick move inventory, inserted {}/{}", blockEntity,
                stack1.getCount() - remaining1.getCount(), stack1.getCount());
        }
        return false;
    }
}
