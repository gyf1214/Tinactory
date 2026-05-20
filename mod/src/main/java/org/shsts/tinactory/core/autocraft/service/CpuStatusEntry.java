package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CpuStatusEntry(
    UUID cpuId,
    JobState state,
    List<CraftAmount> targets,
    int completedSteps,
    int totalSteps,
    ExecutionError error) {

    public CpuStatusEntry {
        targets = List.copyOf(targets);
    }

    public static CpuStatusEntry idle(UUID cpuId) {
        return new CpuStatusEntry(
            cpuId,
            JobState.IDLE,
            List.of(),
            0,
            0,
            ExecutionError.NONE);
    }

    public static CpuStatusEntry offline(UUID cpuId) {
        return new CpuStatusEntry(
            cpuId,
            JobState.FAILED,
            List.of(),
            0,
            0,
            ExecutionError.OFFLINE);
    }
}
