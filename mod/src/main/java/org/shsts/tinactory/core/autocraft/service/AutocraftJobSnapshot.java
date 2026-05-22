package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftJobSnapshot(
    List<CraftAmount> targets,
    JobState state,
    int completedSteps,
    int totalSteps,
    ExecutionError error) {

    public AutocraftJobSnapshot {
        targets = List.copyOf(targets);
    }
}
