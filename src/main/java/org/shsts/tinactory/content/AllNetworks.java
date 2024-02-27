package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllNetworks {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<IScheduling> PRE_WORK_SCHEDULING;
    public static final RegistryEntry<IScheduling> LOGISTICS_SCHEDULING;
    public static final RegistryEntry<IScheduling> ELECTRIC_SCHEDULING;
    public static final RegistryEntry<IScheduling> WORK_SCHEDULING;

    public static final RegistryEntry<ComponentType<ElectricComponent>> ELECTRIC_COMPONENT;

    static {
        PRE_WORK_SCHEDULING = REGISTRATE.scheduling("machine/pre_work").register();
        LOGISTICS_SCHEDULING = REGISTRATE.scheduling("logistics/request").after(() -> PRE_WORK_SCHEDULING).register();
        ELECTRIC_SCHEDULING = REGISTRATE.scheduling("electric").after(() -> PRE_WORK_SCHEDULING).register();
        WORK_SCHEDULING = REGISTRATE.scheduling("machine/work").after(() -> ELECTRIC_SCHEDULING).register();

        ELECTRIC_COMPONENT = REGISTRATE.componentType("electric", ElectricComponent.class, ElectricComponent::new);
    }

    public static void init() {}
}
