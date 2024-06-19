package org.shsts.tinactory.content;

import net.minecraftforge.common.capabilities.CapabilityToken;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.UpdateHelper;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.registrate.common.CapabilityEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final CapabilityEntry<UpdateHelper> UPDATE_HELPER;
    public static final CapabilityEntry<EventManager> EVENT_MANAGER;
    public static final CapabilityEntry<IProcessor> PROCESSOR;
    public static final CapabilityEntry<IContainer> CONTAINER;
    public static final CapabilityEntry<IElectricMachine> ELECTRIC_MACHINE;
    public static final CapabilityEntry<IWorkbench> WORKBENCH;
    public static final CapabilityEntry<IFluidStackHandler> FLUID_STACK_HANDLER;

    public static final CapabilityEntry<Machine> MACHINE;
    public static final CapabilityEntry<MultiBlock> MULTI_BLOCK;
    public static final CapabilityEntry<ElectricChest> ELECTRIC_CHEST;

    static {
        UPDATE_HELPER = REGISTRATE.capability(UpdateHelper.class, new CapabilityToken<>() {});
        EVENT_MANAGER = REGISTRATE.capability(EventManager.class, new CapabilityToken<>() {});
        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        WORKBENCH = REGISTRATE.capability(IWorkbench.class, new CapabilityToken<>() {});
        FLUID_STACK_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});

        MACHINE = REGISTRATE.capability(Machine.class, new CapabilityToken<>() {});
        MULTI_BLOCK = REGISTRATE.capability(MultiBlock.class, new CapabilityToken<>() {});
        ELECTRIC_CHEST = REGISTRATE.capability(ElectricChest.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
