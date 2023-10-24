package org.shsts.tinactory.content;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.WorkbenchContainer;
import org.shsts.tinactory.core.CapabilityProviderType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllCapabilityProviders {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<CapabilityProviderType<BlockEntity, WorkbenchContainer>> WORKBENCH_CONTAINER;

    static {
        WORKBENCH_CONTAINER = REGISTRATE.capabilityProvider("primitive/workbench_container",
                $ -> new WorkbenchContainer());
    }

    public static void init() {}
}
