package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICpuRuntime {
    List<UUID> listVisibleCpus();

    List<UUID> listAvailableCpus();

    Optional<IAutocraftService> findVisibleService(UUID cpuId);
}
