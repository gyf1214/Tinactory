package org.shsts.tinactory.content.network;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.network.Scheduling;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllSchedulings {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<Scheduling> REQUEST_LOGISTICS;
    public static final RegistryEntry<Scheduling> PRE_WORK;
    public static final RegistryEntry<Scheduling> ELECTRIC;
    public static final RegistryEntry<Scheduling> WORK;
    public static final RegistryEntry<Scheduling> SUPPLY_LOGISTICS;

    static {
        REQUEST_LOGISTICS = REGISTRATE.scheduling("logistics/request").register();
        PRE_WORK = REGISTRATE.scheduling("machine/pre_work").after(() -> REQUEST_LOGISTICS).register();
        ELECTRIC = REGISTRATE.scheduling("electric").after(() -> PRE_WORK).register();
        WORK = REGISTRATE.scheduling("machine/work").after(() -> ELECTRIC).register();
        SUPPLY_LOGISTICS = REGISTRATE.scheduling("logistics/supply").after(() -> WORK).register();
    }

    public static void init() {}
}
