package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PlannerSession {
    final List<CraftAmount> targets;
    final PlannerLedger ledger;
    final Map<IIngredientKey, Long> cachedAvailable;
    final List<CraftStep> steps;
    final List<SearchFrame> searchStack;
    int nextTargetIndex;
    long nextStepId;
    @Nullable
    PlanResult result;

    PlannerSession(List<CraftAmount> targets) {
        this.targets = List.copyOf(targets);
        this.ledger = new PlannerLedger();
        this.cachedAvailable = new LinkedHashMap<>();
        this.steps = new ArrayList<>();
        this.searchStack = new ArrayList<>();
        this.nextTargetIndex = 0;
        this.nextStepId = 1L;
        this.result = null;
    }

    static final class SearchFrame {
        final IIngredientKey key;
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

        SearchFrame(IIngredientKey key, long demand, boolean rootDemand) {
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
