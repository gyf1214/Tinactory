package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerSession {
    final List<CraftAmount> targets;
    final PlannerLedger ledger;
    final List<CraftStep> steps;
    int nextTargetIndex;
    long nextStepId;
    @Nullable
    PlanResult result;

    PlannerSession(List<CraftAmount> targets, List<CraftAmount> available) {
        this.targets = List.copyOf(targets);
        this.ledger = new PlannerLedger();
        this.steps = new ArrayList<>();
        for (var resource : available) {
            ledger.add(resource.key(), resource.amount());
        }
        this.nextTargetIndex = 0;
        this.nextStepId = 1L;
        this.result = null;
    }
}
