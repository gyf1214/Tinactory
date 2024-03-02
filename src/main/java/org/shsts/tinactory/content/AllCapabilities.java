package org.shsts.tinactory.content;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.logistics.StackContainer;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.content.machine.RecipeProcessor;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.common.CapabilityProviderType;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.registrate.CapabilityEntry;
import org.shsts.tinactory.registrate.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilities {
    public static final CapabilityEntry<IProcessor> PROCESSOR;
    public static final CapabilityEntry<IContainer> CONTAINER;
    public static final CapabilityEntry<IElectricMachine> ELECTRIC_MACHINE;
    public static final CapabilityEntry<IWorkbench> WORKBENCH;
    public static final CapabilityEntry<IFluidStackHandler> FLUID_STACK_HANDLER;

    public static final RegistryEntry<CapabilityProviderType<BlockEntity, ?>> WORKBENCH_CONTAINER;
    public static final RegistryEntry<CapabilityProviderType<BlockEntity, StackContainer.Builder>>
            STACK_CONTAINER;
    public static final RegistryEntry<CapabilityProviderType<BlockEntity, RecipeProcessor.Builder>>
            RECIPE_PROCESSOR;

    static {
        PROCESSOR = REGISTRATE.capability(IProcessor.class, new CapabilityToken<>() {});
        CONTAINER = REGISTRATE.capability(IContainer.class, new CapabilityToken<>() {});
        ELECTRIC_MACHINE = REGISTRATE.capability(IElectricMachine.class, new CapabilityToken<>() {});
        WORKBENCH = REGISTRATE.capability(IWorkbench.class, new CapabilityToken<>() {});
        FLUID_STACK_HANDLER = REGISTRATE.capability(IFluidStackHandler.class, new CapabilityToken<>() {});

        WORKBENCH_CONTAINER = REGISTRATE.capabilityProvider("primitive/workbench_container",
                Workbench::new);
        STACK_CONTAINER = REGISTRATE.capabilityProvider("logistics/stack_container",
                StackContainer.Builder::new);
        RECIPE_PROCESSOR = REGISTRATE.capabilityProvider("machine/recipe_processor",
                RecipeProcessor.Builder::new);
    }

    public static void init() {}
}
