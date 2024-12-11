package org.shsts.tinactory.content;

import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.ICapability;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final ICapability<IItemHandler> ITEM_HANDLER;
    public static final ICapability<IEventManager> EVENT_MANAGER;

    public static final ICapability<IProcessor> PROCESSOR;
    public static final ICapability<IContainer> CONTAINER;
    public static final ICapability<IElectricMachine> ELECTRIC_MACHINE;
    public static final ICapability<IWorkbench> WORKBENCH;
    public static final ICapability<IFluidStackHandler> FLUID_STACK_HANDLER;
    public static final ICapability<IItemHandlerModifiable> MENU_ITEM_HANDLER;

    public static final ICapability<ILayoutProvider> LAYOUT_PROVIDER;
    public static final ICapability<Machine> MACHINE;
    public static final ICapability<NetworkController> NETWORK_CONTROLLER;
    public static final ICapability<LogisticWorker> LOGISTIC_WORKER;
    public static final ICapability<MultiBlock> MULTI_BLOCK;

    static {
        ITEM_HANDLER = REGISTRATE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        EVENT_MANAGER = REGISTRATE.getCapability(new CapabilityToken<>() {});

        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        WORKBENCH = REGISTRATE.capability(IWorkbench.class, new CapabilityToken<>() {});
        FLUID_STACK_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});
        MENU_ITEM_HANDLER = REGISTRATE.capability(IItemHandlerModifiable.class, new CapabilityToken<>() {});

        LAYOUT_PROVIDER = REGISTRATE.capability(ILayoutProvider.class, new CapabilityToken<>() {});
        MACHINE = REGISTRATE.capability(Machine.class, new CapabilityToken<>() {});
        NETWORK_CONTROLLER = REGISTRATE.capability(NetworkController.class, new CapabilityToken<>() {});
        LOGISTIC_WORKER = REGISTRATE.capability(LogisticWorker.class, new CapabilityToken<>() {});
        MULTI_BLOCK = REGISTRATE.capability(MultiBlock.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
