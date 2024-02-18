package org.shsts.tinactory.content;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.logistics.IProcessingMachine;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.content.machine.ProcessingStackContainer;
import org.shsts.tinactory.content.machine.WorkbenchContainer;
import org.shsts.tinactory.core.common.CapabilityProviderType;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllCapabilities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<Capability<IProcessingMachine>> PROCESSING_MACHINE;
    public static final RegistryEntry<Capability<IWorkbench>> WORKBENCH;
    public static final RegistryEntry<Capability<IFluidStackHandler>> FLUID_STACK_HANDLER;

    public static final RegistryEntry<CapabilityProviderType<BlockEntity, ?>> WORKBENCH_CONTAINER;
    public static final RegistryEntry<CapabilityProviderType<BlockEntity, ProcessingStackContainer.Builder>>
            PROCESSING_STACK_CONTAINER;

    static {
        PROCESSING_MACHINE = REGISTRATE.capability(IProcessingMachine.class, new CapabilityToken<>() {});
        WORKBENCH = REGISTRATE.capability(IWorkbench.class, new CapabilityToken<>() {});
        FLUID_STACK_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});

        WORKBENCH_CONTAINER = REGISTRATE.capabilityProvider("primitive/workbench_container",
                WorkbenchContainer::new);
        PROCESSING_STACK_CONTAINER = REGISTRATE.capabilityProvider("machine/processing_container/stack",
                ProcessingStackContainer.Builder::new);
    }

    public static void init() {}
}
