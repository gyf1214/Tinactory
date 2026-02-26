package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineLease {
    UUID machineId();

    List<IMachineInputRoute> inputRoutes();

    List<IMachineOutputRoute> outputRoutes();

    boolean isValid();

    void release();
}
