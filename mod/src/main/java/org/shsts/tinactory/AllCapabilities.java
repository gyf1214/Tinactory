package org.shsts.tinactory;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.gui.ILayoutProvider;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinactory.integration.logistics.IFluidTanksHandler;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.ICapability;
import org.shsts.tinycorelib.api.registrate.entry.IItemCapability;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinycorelib.api.CoreLibKeys.EVENT_MANAGER_LOC;

public final class AllCapabilities {
    public static final ICapability<IItemHandler> ITEM_HANDLER;
    public static final ICapability<IFluidHandler> FLUID_HANDLER;
    public static final ICapability<IEventManager> EVENT_MANAGER;

    public static final ICapability<IProcessor> PROCESSOR;
    public static final ICapability<IContainer> CONTAINER;
    public static final ICapability<IElectricMachine> ELECTRIC_MACHINE;
    public static final ICapability<IItemHandler> MENU_ITEM_HANDLER;
    public static final ICapability<IFluidTanksHandler> MENU_FLUID_HANDLER;
    public static final ICapability<IItemPort> ITEM_PORT;
    public static final ICapability<IFluidPort> FLUID_PORT;

    public static final ICapability<ILayoutProvider> LAYOUT_PROVIDER;
    public static final ICapability<IMachine> MACHINE;
    public static final ICapability<IBytesProvider> BYTES_PROVIDER;
    public static final ICapability<IPatternCellPort> PATTERN_CELL;
    public static final ICapability<ISignalMachine> SIGNAL_MACHINE;

    public static final IItemCapability<IItemPort> ITEM_PORT_ITEM;
    public static final IItemCapability<IFluidPort> FLUID_PORT_ITEM;
    public static final IItemCapability<IBytesProvider> BYTES_PROVIDER_ITEM;
    public static final IItemCapability<IPatternCellPort> PATTERN_CELL_ITEM;
    public static final IItemCapability<IFluidHandlerItem> FLUID_HANDLER_ITEM;

    static {
        ITEM_HANDLER = REGISTRATE.getCapability(Capabilities.ItemHandler.BLOCK);
        FLUID_HANDLER = REGISTRATE.getCapability(Capabilities.FluidHandler.BLOCK);
        EVENT_MANAGER = REGISTRATE.getCapability(EVENT_MANAGER_LOC, IEventManager.class);

        PROCESSOR = REGISTRATE.capability("processor", IProcessor.class);
        CONTAINER = REGISTRATE.capability("container", IContainer.class);
        ELECTRIC_MACHINE = REGISTRATE.capability("electric_machine", IElectricMachine.class);
        MENU_ITEM_HANDLER = REGISTRATE.capability("menu_item_handler", IItemHandler.class);
        MENU_FLUID_HANDLER = REGISTRATE.capability("menu_fluid_handler", IFluidTanksHandler.class);
        ITEM_PORT = REGISTRATE.capability("item_port", IItemPort.class);
        FLUID_PORT = REGISTRATE.capability("fluid_port", IFluidPort.class);

        LAYOUT_PROVIDER = REGISTRATE.capability("layout_provider", ILayoutProvider.class);
        MACHINE = REGISTRATE.capability("machine", IMachine.class);
        BYTES_PROVIDER = REGISTRATE.capability("bytes_provider", IBytesProvider.class);
        PATTERN_CELL = REGISTRATE.capability("pattern_cell", IPatternCellPort.class);
        SIGNAL_MACHINE = REGISTRATE.capability("signal_machine", ISignalMachine.class);

        ITEM_PORT_ITEM = REGISTRATE.itemCapability("item_port", IItemPort.class);
        FLUID_PORT_ITEM = REGISTRATE.itemCapability("fluid_port", IFluidPort.class);
        BYTES_PROVIDER_ITEM = REGISTRATE.itemCapability("bytes_provider", IBytesProvider.class);
        PATTERN_CELL_ITEM = REGISTRATE.itemCapability("pattern_cell", IPatternCellPort.class);
        FLUID_HANDLER_ITEM = REGISTRATE.getItemCapability(Capabilities.FluidHandler.ITEM);
    }

    public static void init() {}
}
