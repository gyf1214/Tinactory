package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.slf4j.Logger;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.PORT_CLICK;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.machine.Boiler.getHeat;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;
import static org.shsts.tinactory.core.machine.Machine.getProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineMenu extends ProcessingMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final IMachine machine;

    public MachineMenu(Properties properties) {
        super(properties, SLOT_SIZE + SPACING);
        this.machine = MACHINE.get(blockEntity);
        onEventPacket(SET_MACHINE_CONFIG, machine::setConfig);
        onEventPacket(PORT_CLICK, p -> onPortClick(p.getIndex(), p.getButton()));
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private ItemStack clickItemPort(ItemStack carried, IItemCollection collection, int button) {
        if (carried.isEmpty()) {
            var extracted = collection.extractItem(64, true);
            // need to make sure the extracted does not exceed stack size.
            var limit = Math.min(extracted.getCount(), extracted.getMaxStackSize());
            var limit1 = button == 1 ? limit / 2 : limit;
            var extracted1 = extracted.getCount() > limit1 ?
                StackHelper.copyWithCount(extracted, limit1) : extracted;
            return collection.extractItem(extracted1, false);
        } else {
            if (button == 1) {
                var carried1 = StackHelper.copyWithCount(carried, 1);
                var remaining = collection.insertItem(carried1, false);
                if (remaining.isEmpty()) {
                    carried.shrink(1);
                }
                return carried;
            } else {
                return collection.insertItem(carried, false);
            }
        }
    }

    private boolean tryDrain(IFluidHandlerItem handler, IFluidCollection collection, FluidStack fluid) {
        var fluid1 = fluid.isEmpty() ? collection.drain(Integer.MAX_VALUE, true) :
            collection.drain(StackHelper.copyWithAmount(fluid, Integer.MAX_VALUE), true);
        int amount = handler.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
        if (amount > 0) {
            var fluid2 = StackHelper.copyWithAmount(fluid1, amount);
            var fluid3 = collection.drain(fluid2, false);
            var amount1 = handler.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
            if (amount1 != amount) {
                LOGGER.warn("Failed to execute fluid drain extracted={}/{}", amount1, amount);
            }
            return true;
        }
        return false;
    }

    private FluidClickResult doClickFluidPort(ItemStack carried, IFluidCollection collection,
        boolean mayDrain, boolean mayFill) {
        var cap = StackHelper.getFluidHandlerFromItem(carried);
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        if (mayFill) {
            var fluid1 = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (!fluid1.isEmpty()) {
                int amount = collection.fill(fluid1, true);
                if (amount > 0) {
                    var fluid2 = StackHelper.copyWithAmount(fluid1, amount);
                    var fluid3 = handler.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                    var amount1 = collection.fill(fluid3, false);
                    if (amount1 != amount) {
                        LOGGER.warn("Failed to execute fluid fill inserted={}/{}", amount1, amount);
                    }
                    return new FluidClickResult(FluidClickAction.FILL, handler.getContainer());
                }
            }
        }
        if (mayDrain) {
            for (var i = 0; i < handler.getTanks(); i++) {
                var fluid1 = handler.getFluidInTank(i);
                if (!fluid1.isEmpty() && tryDrain(handler, collection, fluid1)) {
                    return new FluidClickResult(FluidClickAction.DRAIN, handler.getContainer());
                }
            }
            if (tryDrain(handler, collection, FluidStack.EMPTY)) {
                return new FluidClickResult(FluidClickAction.DRAIN, handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    private Optional<IPort> getPort(int port) {
        return machine.container().flatMap($ -> $.hasPort(port) ?
            Optional.of($.getPort(port, ContainerAccess.MENU)) : Optional.empty());
    }

    private void onPortClick(int port, int button) {
        getPort(port).ifPresent(port1 -> {
            if (port1.type() == PortType.ITEM) {
                var carried1 = clickItemPort(getCarried(), port1.asItem(), button);
                setCarried(carried1);
            } else if (port1.type() == PortType.FLUID) {
                clickFluidSlot((carried, mayDrain, mayFill) ->
                    doClickFluidPort(carried, port1.asFluid(), mayDrain, mayFill), button);
            }
        });
    }

    public static class Simple extends LayoutMenu {
        private Simple(Properties properties, int extraHeight) {
            super(properties, extraHeight);
            addLayoutSlots(layout);
            onEventPacket(SET_MACHINE_CONFIG, this::setMachineConfig);
        }

        @Override
        public boolean stillValid(Player player) {
            return super.stillValid(player) && MACHINE.tryGet(blockEntity)
                .filter($ -> $.canPlayerInteract(player))
                .isPresent();
        }

        private void setMachineConfig(ISetMachineConfigPacket packet) {
            MACHINE.tryGet(blockEntity).ifPresent($ -> $.setConfig(packet));
        }
    }

    public static class Boiler extends MachineMenu {
        public Boiler(Properties properties) {
            super(properties);
            addSyncSlot("burn", () -> doublePacket(getProcessor(blockEntity)
                .map(IProcessor::getProgress)
                .orElse(0d)));
            addSyncSlot("heat", () -> doublePacket(getProcessor(blockEntity)
                .map($ -> getHeat($) / 500d)
                .orElse(0d)));
        }
    }

    public static LayoutMenu simpleConfig(Properties properties) {
        return new Simple(properties, SLOT_SIZE + SPACING);
    }

    public static ProcessingMenu machine(Properties properties) {
        return new MachineMenu(properties);
    }

    public static ProcessingMenu boiler(Properties properties) {
        return new Boiler(properties);
    }

    public static ProcessingMenu digitalInterface(Properties properties) {
        return new DigitalInterfaceMenu(properties);
    }
}
