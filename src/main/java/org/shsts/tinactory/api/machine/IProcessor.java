package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IProcessor {
    /**
     * Must be called from Server.
     */
    void onPreWork();

    /**
     * Must be called from Server.
     */
    void onWorkTick(double partial);

    double getProgress();
}
