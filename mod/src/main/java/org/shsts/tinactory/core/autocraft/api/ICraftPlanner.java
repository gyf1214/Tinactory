package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlannerSnapshot;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftPlanner {
    PlannerSnapshot plan(List<CraftAmount> targets);
}
