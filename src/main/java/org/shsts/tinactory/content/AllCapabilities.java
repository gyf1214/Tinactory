package org.shsts.tinactory.content;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.IProcessingMachine;
import org.shsts.tinactory.content.machine.WorkbenchContainer;
import org.shsts.tinactory.core.CapabilityProviderType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllCapabilities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<Capability<IProcessingMachine>> PROCESSING_MACHINE;
    public static final RegistryEntry<CapabilityProviderType<BlockEntity, WorkbenchContainer>> WORKBENCH_CONTAINER;

    static {
        PROCESSING_MACHINE = REGISTRATE.capability(IProcessingMachine.class, new CapabilityToken<>() {});
        WORKBENCH_CONTAINER = REGISTRATE.capabilityProvider("primitive/workbench_container",
                WorkbenchContainer::new);
    }

    public static void init() {}
}
