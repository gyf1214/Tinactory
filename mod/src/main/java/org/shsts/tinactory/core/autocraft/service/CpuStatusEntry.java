package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CpuStatusEntry(
    UUID cpuId,
    boolean available,
    List<CraftAmount> targets,
    JobState state,
    @Nullable ExecutionPhase phase,
    int nextStepIndex,
    int stepCount,
    ExecutionError error,
    boolean cancellable) {

    public CpuStatusEntry {
        targets = List.copyOf(targets);
    }

    public static CpuStatusEntry idle(UUID cpuId, boolean available) {
        return new CpuStatusEntry(
            cpuId,
            available,
            List.of(),
            JobState.IDLE,
            null,
            0,
            0,
            ExecutionError.NONE,
            false);
    }
}
