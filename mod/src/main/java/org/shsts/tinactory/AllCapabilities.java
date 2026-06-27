package org.shsts.tinactory;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
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
import org.shsts.tinactory.integration.logistics.IMenuItemHandler;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.ICapability;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final ICapability<IItemHandler> ITEM_HANDLER;
    public static final ICapability<IFluidHandler> FLUID_HANDLER;
    public static final ICapability<IEventManager> EVENT_MANAGER;

    public static final ICapability<IProcessor> PROCESSOR;
    public static final ICapability<IContainer> CONTAINER;
    public static final ICapability<IElectricMachine> ELECTRIC_MACHINE;
    public static final ICapability<IMenuItemHandler> MENU_ITEM_HANDLER;
    public static final ICapability<IFluidTanksHandler> MENU_FLUID_HANDLER;
    public static final ICapability<IItemPort> ITEM_PORT;
    public static final ICapability<IFluidPort> FLUID_PORT;

    public static final ICapability<ILayoutProvider> LAYOUT_PROVIDER;
    public static final ICapability<IMachine> MACHINE;
    public static final ICapability<IBytesProvider> BYTES_PROVIDER;
    public static final ICapability<IPatternCellPort> PATTERN_CELL;
    public static final ICapability<ISignalMachine> SIGNAL_MACHINE;

    static {
        ITEM_HANDLER = REGISTRATE.capability(Capabilities.ItemHandler.BLOCK);
        FLUID_HANDLER = REGISTRATE.capability(Capabilities.FluidHandler.BLOCK);
        EVENT_MANAGER = REGISTRATE.capability("event_manager", IEventManager.class);

        PROCESSOR = REGISTRATE.capability("processor", IProcessor.class);
        CONTAINER = REGISTRATE.capability("container", IContainer.class);
        ELECTRIC_MACHINE = REGISTRATE.capability("electric_machine", IElectricMachine.class);
        MENU_ITEM_HANDLER = REGISTRATE.capability("menu_item_handler", IMenuItemHandler.class);
        MENU_FLUID_HANDLER = REGISTRATE.capability("menu_fluid_handler", IFluidTanksHandler.class);
        ITEM_PORT = REGISTRATE.capability("item_port", IItemPort.class);
        FLUID_PORT = REGISTRATE.capability("fluid_port", IFluidPort.class);

        LAYOUT_PROVIDER = REGISTRATE.capability("layout_provider", ILayoutProvider.class);
        MACHINE = REGISTRATE.capability("machine", IMachine.class);
        BYTES_PROVIDER = REGISTRATE.capability("bytes_provider", IBytesProvider.class);
        PATTERN_CELL = REGISTRATE.capability("pattern_cell", IPatternCellPort.class);
        SIGNAL_MACHINE = REGISTRATE.capability("signal_machine", ISignalMachine.class);
    }

    public static void init() {}
}
