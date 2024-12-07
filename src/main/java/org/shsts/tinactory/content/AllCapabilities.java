package org.shsts.tinactory.content;

import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.UpdateHelper;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinycorelib.api.registrate.entry.ICapability;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final ICapability<UpdateHelper> UPDATE_HELPER;
    public static final ICapability<EventManager> EVENT_MANAGER;
    public static final ICapability<IProcessor> PROCESSOR;
    public static final ICapability<IContainer> CONTAINER;
    public static final ICapability<IElectricMachine> ELECTRIC_MACHINE;
    public static final ICapability<IWorkbench> WORKBENCH;
    public static final ICapability<IFluidStackHandler> FLUID_STACK_HANDLER;
    public static final ICapability<IItemHandlerModifiable> MENU_ITEM_HANDLER;

    public static final ICapability<Machine> MACHINE;
    public static final ICapability<MultiBlock> MULTI_BLOCK;

    static {
        UPDATE_HELPER = REGISTRATE.capability(UpdateHelper.class, new CapabilityToken<>() {});
        EVENT_MANAGER = REGISTRATE.capability(EventManager.class, new CapabilityToken<>() {});
        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        WORKBENCH = REGISTRATE.capability(IWorkbench.class, new CapabilityToken<>() {});
        FLUID_STACK_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});
        MENU_ITEM_HANDLER = REGISTRATE.capability(IItemHandlerModifiable.class, new CapabilityToken<>() {});

        MACHINE = REGISTRATE.capability(Machine.class, new CapabilityToken<>() {});
        MULTI_BLOCK = REGISTRATE.capability(MultiBlock.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
