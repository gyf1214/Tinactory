package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlannerSnapshot;
import org.shsts.tinactory.core.autocraft.plan.PlannerSession;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IIncrementalCraftPlanner extends ICraftPlanner {
    PlannerSession startSession(List<CraftAmount> targets);

    PlannerSnapshot resume(PlannerSession session, int stepBudget);
}
