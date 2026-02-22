package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlanError;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftJob(
    UUID id,
    List<CraftAmount> targets,
    Status status,
    @Nullable PlanError planError,
    @Nullable ExecutionError executionError) {
    public AutocraftJob {
        targets = List.copyOf(targets);
    }

    public enum Status {
        QUEUED,
        RUNNING,
        BLOCKED,
        FAILED,
        CANCELLED,
        DONE
    }
}
