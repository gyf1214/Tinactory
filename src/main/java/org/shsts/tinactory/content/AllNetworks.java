package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.SignalComponent;
import org.shsts.tinactory.core.builder.SchedulingBuilder;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRegistries.COMPONENT_TYPES;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllNetworks {
    public static final IEntry<IScheduling> PRE_WORK_SCHEDULING;
    public static final IEntry<IScheduling> WORK_SCHEDULING;
    public static final IEntry<IScheduling> POST_WORK_SCHEDULING;
    public static final IEntry<IScheduling> LOGISTICS_SCHEDULING;
    public static final IEntry<IScheduling> ELECTRIC_SCHEDULING;
    public static final IEntry<IScheduling> SIGNAL_READ_SCHEDULING;
    public static final IEntry<IScheduling> SIGNAL_WRITE_SCHEDULING;

    public static final IEntry<ComponentType<ElectricComponent>> ELECTRIC_COMPONENT;
    public static final IEntry<ComponentType<LogisticComponent>> LOGISTIC_COMPONENT;
    public static final IEntry<ComponentType<SignalComponent>> SIGNAL_COMPONENT;

    static {
        PRE_WORK_SCHEDULING = scheduling("machine/pre_work").register();
        WORK_SCHEDULING = scheduling("machine/work").after(PRE_WORK_SCHEDULING).register();
        POST_WORK_SCHEDULING = scheduling("machine/post_work").after(WORK_SCHEDULING).register();
        LOGISTICS_SCHEDULING = scheduling("logistics").before(PRE_WORK_SCHEDULING).register();
        ELECTRIC_SCHEDULING = scheduling("electric")
            .after(PRE_WORK_SCHEDULING)
            .before(WORK_SCHEDULING)
            .register();
        SIGNAL_READ_SCHEDULING = scheduling("machine/signal_read")
            .after(POST_WORK_SCHEDULING)
            .register();
        SIGNAL_WRITE_SCHEDULING = scheduling("machine/signal_write")
            .before(LOGISTICS_SCHEDULING)
            .register();

        ELECTRIC_COMPONENT = componentType("electric", ElectricComponent.class, ElectricComponent::new);
        LOGISTIC_COMPONENT = componentType("logistics", LogisticComponent.class, LogisticComponent::new);
        SIGNAL_COMPONENT = componentType("machine/signal", SignalComponent.class, SignalComponent::new);
    }

    public static void init() {}

    private static <T extends NetworkComponent> IEntry<ComponentType<T>> componentType(
        String id, Class<T> clazz, NetworkComponent.Factory<T> factory) {
        return REGISTRATE.registryEntry(COMPONENT_TYPES.getHandler(), id,
            () -> new ComponentType<>(clazz, factory));
    }

    private static SchedulingBuilder<IRegistrate> scheduling(String id) {
        return new SchedulingBuilder<>(REGISTRATE, REGISTRATE, id);
    }
}
