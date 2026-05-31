package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.JobState;
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
    ExecutionError error,
    long memoryLimit,
    long memoryUsage) {

    public CpuStatusEntry {
        targets = List.copyOf(targets);
    }

    public CpuStatusEntry(
        UUID cpuId,
        JobState state,
        List<CraftAmount> targets,
        int completedSteps,
        int totalSteps,
        ExecutionError error) {

        this(cpuId, state, targets, completedSteps, totalSteps, error, 0L, 0L);
    }

    public static CpuStatusEntry idle(UUID cpuId, long memoryLimit) {
        return new CpuStatusEntry(
            cpuId,
            JobState.IDLE,
            List.of(),
            0,
            0,
            ExecutionError.NONE,
            memoryLimit,
            0L);
    }

    public static CpuStatusEntry idle(UUID cpuId) {
        return idle(cpuId, 0L);
    }

    public static CpuStatusEntry offline(UUID cpuId) {
        return new CpuStatusEntry(
            cpuId,
            JobState.FAILED,
            List.of(),
            0,
            0,
            ExecutionError.OFFLINE,
            0L,
            0L);
    }

    public boolean available() {
        return state == JobState.IDLE;
    }
}
