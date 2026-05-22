package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlanResult;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ICraftPlanner {
    void start(List<CraftAmount> targets);

    Optional<PlanResult> advance(int budget);

    default PlanResult plan(List<CraftAmount> targets) {
        start(targets);
        while (true) {
            var result = advance(Integer.MAX_VALUE);
            if (result.isPresent()) {
                return result.get();
            }
        }
    }
}
