package org.shsts.tinactory.registrate;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.common.ICapabilityProviderType;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.network.SchedulingManager;

public final class AllRegistries {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final SmartRegistry<IScheduling> SCHEDULING_REGISTRY;
    public static final SmartRegistry<ICapabilityProviderType<?, ?>> CAPABILITY_PROVIDER_TYPE_REGISTRY;

    static {
        SCHEDULING_REGISTRY = REGISTRATE.registry("scheduling", IScheduling.class)
                .onBake(SchedulingManager::onBake)
                .register();
        CAPABILITY_PROVIDER_TYPE_REGISTRY = REGISTRATE.simpleRegistry("capability_provider_type",
                ICapabilityProviderType.class);
    }

    public static void init() {}
}
