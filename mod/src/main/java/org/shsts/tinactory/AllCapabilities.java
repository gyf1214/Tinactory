package org.shsts.tinactory;

import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.autocraft.integration.IPatternCellPort;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.integration.logistics.IFluidTanksHandler;
import org.shsts.tinactory.integration.logistics.IMenuItemHandler;
import org.shsts.tinactory.core.logistics.ISignalMachine;
import org.shsts.tinactory.core.machine.ILayoutProvider;
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
    public static final ICapability<IDigitalProvider> DIGITAL_PROVIDER;
    public static final ICapability<IPatternCellPort> PATTERN_CELL;
    public static final ICapability<ISignalMachine> SIGNAL_MACHINE;

    static {
        ITEM_HANDLER = REGISTRATE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        FLUID_HANDLER = REGISTRATE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
        EVENT_MANAGER = REGISTRATE.getCapability(new CapabilityToken<>() {});

        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        MENU_ITEM_HANDLER = REGISTRATE.capability(IMenuItemHandler.class, new CapabilityToken<>() {});
        MENU_FLUID_HANDLER = REGISTRATE.capability(IFluidTanksHandler.class, new CapabilityToken<>() {});
        ITEM_PORT = REGISTRATE.capability(IItemPort.class, new CapabilityToken<>() {});
        FLUID_PORT = REGISTRATE.capability(IFluidPort.class, new CapabilityToken<>() {});

        LAYOUT_PROVIDER = REGISTRATE.capability(ILayoutProvider.class, new CapabilityToken<>() {});
        MACHINE = REGISTRATE.capability(IMachine.class, new CapabilityToken<>() {});
        BYTES_PROVIDER = REGISTRATE.capability(IBytesProvider.class, new CapabilityToken<>() {});
        DIGITAL_PROVIDER = REGISTRATE.capability(IDigitalProvider.class, new CapabilityToken<>() {});
        PATTERN_CELL = REGISTRATE.capability(IPatternCellPort.class, new CapabilityToken<>() {});
        SIGNAL_MACHINE = REGISTRATE.capability(ISignalMachine.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
