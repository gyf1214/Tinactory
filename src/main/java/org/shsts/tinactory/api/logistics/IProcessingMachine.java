package org.shsts.tinactory.api.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IProcessingMachine {
    /**
     * Must be called from Server.
     */
    void onWorkTick(double partial);

    boolean hasPort(int port);

    IPort getPort(int port, boolean internal);

    double getProgress();
}
