package org.shsts.tinactory.registrate;

import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.SchedulingManager;
import org.shsts.tinactory.registrate.common.SmartRegistry;

import static org.shsts.tinactory.Tinactory._REGISTRATE;

public final class AllRegistries {
    public static final SmartRegistry<IScheduling> SCHEDULING_REGISTRY;
    public static final SmartRegistry<ComponentType<?>> COMPONENT_TYPE_REGISTRY;
    public static final SmartRegistry<Event<?>> EVENT;

    static {
        SCHEDULING_REGISTRY = _REGISTRATE.registry("scheduling", IScheduling.class)
            .onBake(SchedulingManager::onBake)
            .register();
        COMPONENT_TYPE_REGISTRY = _REGISTRATE.<ComponentType<?>>genericRegistry("component_type", ComponentType.class)
            .onBake((registry, stage) -> ComponentType.onBake(registry))
            .register();
        EVENT = _REGISTRATE.simpleRegistry("event", Event.class);
    }

    public static void init() {}
}
