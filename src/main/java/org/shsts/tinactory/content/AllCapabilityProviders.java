package org.shsts.tinactory.content;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.logistics.StackContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.RecipeProcessor;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.common.CapabilityProviderType;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllCapabilityProviders {
    public static final RegistryEntry<CapabilityProviderType<BlockEntity, ?>> WORKBENCH_CONTAINER;
    public static final RegistryEntry<CapabilityProviderType<Machine, StackContainer.Builder>>
            STACK_CONTAINER;
    public static final RegistryEntry<CapabilityProviderType<Machine, RecipeProcessor.Builder>>
            RECIPE_PROCESSOR;

    static {
        WORKBENCH_CONTAINER = REGISTRATE.capabilityProvider("primitive/workbench_container",
                Workbench::new);
        STACK_CONTAINER = REGISTRATE.capabilityProvider("logistics/stack_container",
                StackContainer.Builder::new);
        RECIPE_PROCESSOR = REGISTRATE.capabilityProvider("machine/recipe_processor",
                RecipeProcessor.Builder::new);
    }

    public static void init() {}
}
