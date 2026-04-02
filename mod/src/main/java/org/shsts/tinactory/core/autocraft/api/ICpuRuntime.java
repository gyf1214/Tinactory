package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICpuRuntime {
    void registerCpu(IMachine machine, IAutocraftService service);

    void unregisterCpu(UUID cpuId);

    List<UUID> listVisibleCpus();

    List<UUID> listAvailableCpus();

    Optional<IAutocraftService> findVisibleService(UUID cpuId);
}
