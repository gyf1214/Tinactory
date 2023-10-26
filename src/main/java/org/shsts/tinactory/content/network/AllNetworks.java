package org.shsts.tinactory.content.network;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.network.Scheduling;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllNetworks {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<Scheduling> PRE_WORK;
    public static final RegistryEntry<Scheduling> LOGISTICS;
    public static final RegistryEntry<Scheduling> ELECTRIC;
    public static final RegistryEntry<Scheduling> WORK;

    static {
        PRE_WORK = REGISTRATE.scheduling("machine/pre_work").register();
        LOGISTICS = REGISTRATE.scheduling("logistics/request").after(() -> PRE_WORK).register();
        ELECTRIC = REGISTRATE.scheduling("electric").after(() -> PRE_WORK).register();
        WORK = REGISTRATE.scheduling("machine/work").after(() -> ELECTRIC).register();
    }

    public static void init() {
    }
}
