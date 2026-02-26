package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IIncrementalCraftPlanner {
    PlannerSession startSession(List<CraftAmount> targets, List<CraftAmount> available);

    PlannerProgress resume(PlannerSession session, int stepBudget);
}
