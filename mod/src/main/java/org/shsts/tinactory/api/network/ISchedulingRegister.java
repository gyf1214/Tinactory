package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISchedulingRegister {
    void add(IScheduling scheduling, INetworkTicker ticker);
}
