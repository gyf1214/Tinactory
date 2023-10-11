package org.shsts.tinactory.registrate;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.network.Scheduling;

public final class AllRegistries {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final SmartRegistry<Scheduling> SCHEDULING_REGISTRY;

    static {
        SCHEDULING_REGISTRY = REGISTRATE.registry("scheduling", Scheduling.class)
                .register();
    }

    public static void init() {}
}
