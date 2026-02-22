package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerSession {
    final List<CraftAmount> targets;
    final PlannerLedger ledger;
    final List<CraftStep> steps;
    final List<SearchFrame> searchStack;
    int nextTargetIndex;
    long nextStepId;
    @Nullable
    PlanResult result;

    PlannerSession(List<CraftAmount> targets, List<CraftAmount> available) {
        this.targets = List.copyOf(targets);
        this.ledger = new PlannerLedger();
        this.steps = new ArrayList<>();
        this.searchStack = new ArrayList<>();
        for (var resource : available) {
            ledger.add(resource.key(), resource.amount());
        }
        this.nextTargetIndex = 0;
        this.nextStepId = 1L;
        this.result = null;
    }

    static final class SearchFrame {
        final CraftKey key;
        final long demand;
        final boolean rootDemand;
        long remaining;
        List<CraftPattern> candidates;
        int candidateIndex;
        int inputIndex;
        long runs;
        PlannerLedger ledgerSnapshot;
        int stepCountSnapshot;
        long stepIdSnapshot;
        @Nullable
        PlanError firstError;
        @Nullable
        PlanError childError;
        Stage stage;

        SearchFrame(CraftKey key, long demand, boolean rootDemand) {
            this.key = key;
            this.demand = demand;
            this.rootDemand = rootDemand;
            this.remaining = 0L;
            this.candidates = List.of();
            this.candidateIndex = 0;
            this.inputIndex = 0;
            this.runs = 0L;
            this.ledgerSnapshot = new PlannerLedger();
            this.stepCountSnapshot = 0;
            this.stepIdSnapshot = 1L;
            this.firstError = null;
            this.childError = null;
            this.stage = Stage.START;
        }
    }

    enum Stage {
        START,
        SELECT_PATTERN,
        REDUCE_INPUTS,
        APPLY_PATTERN
    }
}
