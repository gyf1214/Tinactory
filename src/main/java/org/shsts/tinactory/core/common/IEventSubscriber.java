package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IEventSubscriber {
    void subscribeEvents(EventManager eventManager);
}
