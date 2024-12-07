package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.Tinactory._REGISTRATE;
import static org.shsts.tinactory.registrate.AllRegistries.COMPONENT_TYPES;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllNetworks {
    public static final RegistryEntry<IScheduling> PRE_WORK_SCHEDULING;
    public static final RegistryEntry<IScheduling> LOGISTICS_SCHEDULING;
    public static final RegistryEntry<IScheduling> ELECTRIC_SCHEDULING;
    public static final RegistryEntry<IScheduling> WORK_SCHEDULING;

    public static final IEntry<ComponentType<ElectricComponent>> ELECTRIC_COMPONENT;
    public static final IEntry<ComponentType<LogisticComponent>> LOGISTIC_COMPONENT;

    static {
        PRE_WORK_SCHEDULING = _REGISTRATE.scheduling("machine/pre_work").register();
        LOGISTICS_SCHEDULING = _REGISTRATE.scheduling("logistics").after(() -> PRE_WORK_SCHEDULING).register();
        ELECTRIC_SCHEDULING = _REGISTRATE.scheduling("electric").after(() -> PRE_WORK_SCHEDULING).register();
        WORK_SCHEDULING = _REGISTRATE.scheduling("machine/work").after(() -> ELECTRIC_SCHEDULING).register();

        ELECTRIC_COMPONENT = componentType("electric", ElectricComponent.class, ElectricComponent::new);
        LOGISTIC_COMPONENT = componentType("logistics", LogisticComponent.class, LogisticComponent::new);
    }

    public static void init() {}

    public static <T extends NetworkComponent> IEntry<ComponentType<T>> componentType(
        String id, Class<T> clazz, NetworkComponent.Factory<T> factory) {
        return REGISTRATE.registryEntry(COMPONENT_TYPES.getHandler(), id,
            () -> new ComponentType<>(clazz, factory));
    }
}
