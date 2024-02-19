package org.shsts.tinactory.api.machine;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IProcessor {
    /**
     * Must be called from Server.
     */
    void onWorkTick(double partial);

    double getProgress();
}
