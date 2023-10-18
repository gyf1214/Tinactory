package org.shsts.tinactory.registrate;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.CapabilityProviderType;
import org.shsts.tinactory.network.Scheduling;
import org.shsts.tinactory.network.SchedulingManager;

public final class AllRegistries {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final SmartRegistry<Scheduling> SCHEDULING_REGISTRY;
    public static final SmartRegistry<CapabilityProviderType<?, ?>> CAPABILITY_PROVIDER_TYPE_REGISTRY;

    static {
        SCHEDULING_REGISTRY = REGISTRATE.registry("scheduling", Scheduling.class)
                .onBake(SchedulingManager::onBake)
                .register();
        CAPABILITY_PROVIDER_TYPE_REGISTRY = REGISTRATE.simpleRegistry("capability_provider_type",
                CapabilityProviderType.class);
    }

    public static void init() {}
}
