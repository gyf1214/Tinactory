package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;
import org.shsts.tinactory.core.autocraft.service.AutocraftJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IAutocraftService {
    boolean isBusy();

    Optional<AutocraftJob> getJob();

    boolean cancel(UUID id);

    UUID submitPrepared(List<CraftAmount> targets, CraftPlan plan);

    Optional<Integer> runningPlanStepCount();
}
