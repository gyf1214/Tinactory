package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllNetworks {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<IScheduling> PRE_WORK;
    public static final RegistryEntry<IScheduling> LOGISTICS;
    public static final RegistryEntry<IScheduling> ELECTRIC;
    public static final RegistryEntry<IScheduling> WORK;

    static {
        PRE_WORK = REGISTRATE.scheduling("machine/pre_work").register();
        LOGISTICS = REGISTRATE.scheduling("logistics/request").after(() -> PRE_WORK).register();
        ELECTRIC = REGISTRATE.scheduling("electric").after(() -> PRE_WORK).register();
        WORK = REGISTRATE.scheduling("machine/work").after(() -> ELECTRIC).register();
    }

    public static void init() {
    }
}
