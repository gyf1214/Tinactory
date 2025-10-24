package org.shsts.tinactory.content;

import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.ISignalMachine;
import org.shsts.tinactory.core.logistics.IDigitalProvider;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.ICapability;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final ICapability<IItemHandler> ITEM_HANDLER;
    public static final ICapability<IEventManager> EVENT_MANAGER;

    public static final ICapability<IProcessor> PROCESSOR;
    public static final ICapability<IContainer> CONTAINER;
    public static final ICapability<IElectricMachine> ELECTRIC_MACHINE;
    public static final ICapability<IItemHandlerModifiable> MENU_ITEM_HANDLER;
    public static final ICapability<IFluidStackHandler> MENU_FLUID_HANDLER;
    public static final ICapability<IItemCollection> ITEM_COLLECTION;
    public static final ICapability<IFluidCollection> FLUID_COLLECTION;

    public static final ICapability<ILayoutProvider> LAYOUT_PROVIDER;
    public static final ICapability<IMachine> MACHINE;
    public static final ICapability<IDigitalProvider> DIGITAL_PROVIDER;
    public static final ICapability<ISignalMachine> SIGNAL_MACHINE;

    static {
        ITEM_HANDLER = REGISTRATE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        EVENT_MANAGER = REGISTRATE.getCapability(new CapabilityToken<>() {});

        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        MENU_ITEM_HANDLER = REGISTRATE.capability(IItemHandlerModifiable.class, new CapabilityToken<>() {});
        MENU_FLUID_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});
        ITEM_COLLECTION = REGISTRATE.capability(IItemCollection.class, new CapabilityToken<>() {});
        FLUID_COLLECTION = REGISTRATE.capability(IFluidCollection.class, new CapabilityToken<>() {});

        LAYOUT_PROVIDER = REGISTRATE.capability(ILayoutProvider.class, new CapabilityToken<>() {});
        MACHINE = REGISTRATE.capability(IMachine.class, new CapabilityToken<>() {});
        DIGITAL_PROVIDER = REGISTRATE.capability(IDigitalProvider.class, new CapabilityToken<>() {});
        SIGNAL_MACHINE = REGISTRATE.capability(ISignalMachine.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
