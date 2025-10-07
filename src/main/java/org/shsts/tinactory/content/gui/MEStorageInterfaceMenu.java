package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceEventPacket;
import org.shsts.tinactory.content.gui.sync.MEStorageInterfaceSyncPacket;
import org.shsts.tinactory.content.machine.MEStorageInterface;
import org.shsts.tinactory.core.gui.InventoryMenu;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.slf4j.Logger;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.ME_STORAGE_INTERFACE_SLOT;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceMenu extends InventoryMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String SLOT_SYNC = "slots";
    public static final int PANEL_HEIGHT = 6 * SLOT_SIZE + 21;

    private final IMachine machine;
    private final IMachineConfig machineConfig;
    private final MEStorageInterface storageInterface;
    private boolean needUpdate = true;
    private final Runnable updateListener = () -> needUpdate = true;

    private class SyncScheduler implements ISyncSlotScheduler<MEStorageInterfaceSyncPacket> {
        @Override
        public boolean shouldSend() {
            return needUpdate;
        }

        @Override
        public MEStorageInterfaceSyncPacket createPacket() {
            needUpdate = false;
            return new MEStorageInterfaceSyncPacket(
                storageInterface.getAllItems(), storageInterface.getAllFluids());
        }
    }

    public MEStorageInterfaceMenu(Properties properties) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity);
        this.machineConfig = machine.config();
        this.storageInterface = getProvider(blockEntity, MEStorageInterface.ID, MEStorageInterface.class);

        addSyncSlot(SLOT_SYNC, new SyncScheduler());
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
            if (button == 0) {
                setCarried(port.insertItem(carried, false));
            } else {
                var carried1 = StackHelper.copyWithCount(carried, 1);
                carried.shrink(1);
                var remaining = port.insertItem(carried1, false);
                var combined = StackHelper.combineStack(carried, remaining);
                if (combined.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, remaining);
                } else {
                    setCarried(combined.get());
                }
            }
        } else {
            var count = Math.min(item.getCount(), item.getMaxStackSize());
            var count1 = button == 0 ? count : (count + 1) / 2;
            var item1 = StackHelper.copyWithCount(item, count1);
            var extracted = port.extractItem(item1, false);
            setCarried(extracted);
        }
    }

    private void onSlotClick(MEStorageInterfaceEventPacket packet) {
        var button = packet.button();
        var fluidPort = storageInterface.fluidPort();
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
}
