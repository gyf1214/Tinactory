package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ICraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GoalReductionPlanner implements ICraftPlanner {
    private final IPatternRepository patterns;
    private final IInventoryView inventory;
    private final AutocraftMemoryConfig memoryConfig;
    @Nullable
    private PlanningSession activeSession;

    public GoalReductionPlanner(IPatternRepository patterns, IInventoryView inventory) {
        this(patterns, inventory, AutocraftMemoryConfig.NONE);
    }

    public GoalReductionPlanner(
        IPatternRepository patterns,
        IInventoryView inventory,
        AutocraftMemoryConfig memoryConfig) {

        this.patterns = patterns;
        this.inventory = inventory;
        this.memoryConfig = memoryConfig;
    }

    @Override
    public void start(List<CraftAmount> targets) {
        activeSession = new PlanningSession(targets);
    }

    @Override
    public Optional<PlanResult> advance(int stepBudget) {
        if (activeSession == null) {
            return Optional.empty();
        }
        return resume(activeSession, stepBudget);
    }

    private Optional<PlanResult> resume(PlanningSession session, int stepBudget) {
        if (session.result != null) {
            return Optional.of(session.result);
        }
        if (stepBudget <= 0) {
            return Optional.empty();
        }

        var budget = stepBudget;
        while (budget > 0 && session.result == null) {
            if (session.searchStack.isEmpty()) {
                if (session.nextTargetIndex >= session.targets.size()) {
                    session.result = PlanResult.completed(buildPlan(session));
                    return Optional.of(session.result);
                }
                var target = session.targets.get(session.nextTargetIndex);
                session.searchStack.add(new SearchFrame(target.key(), target.amount(), true));
            }
            processOneSearchStep(session);
            budget--;
        }
        if (session.result != null) {
            return Optional.of(session.result);
        }
        if (session.nextTargetIndex >= session.targets.size() && session.searchStack.isEmpty()) {
            session.result = PlanResult.completed(buildPlan(session));
            return Optional.of(session.result);
        }
        return Optional.empty();
    }

    private void processOneSearchStep(PlanningSession session) {
        var frame = peekFrame(session);
        switch (frame.stage) {
            case START:
                runStartStage(session, frame);
                break;
            case SELECT_PATTERN:
                runSelectPatternStage(session, frame);
                break;
            case REDUCE_INPUTS:
                runReduceInputsStage(session, frame);
                break;
            case APPLY_PATTERN:
                runApplyPatternStage(session, frame);
                break;
        }
    }

    private void runStartStage(PlanningSession session, SearchFrame frame) {
        loadAvailableAmount(session, frame.key);
        var consumed = frame.rootDemand ?
            session.ledger.consumeCrafted(frame.key, frame.demand) :
            session.ledger.consume(frame.key, frame.demand);
        frame.remaining = frame.demand - consumed;
        if (frame.remaining <= 0L) {
            popSuccess(session);
            return;
        }
        var cycleError = detectCycle(session.searchStack);
        if (cycleError != null) {
            session.ledger.recordUnsatisfiedInventoryDemand(frame.key, frame.remaining);
            popFailure(session, cycleError);
            return;
        }
        frame.candidates = choosePatterns(frame.key);
        if (frame.candidates.isEmpty()) {
            var error = frame.rootDemand ?
                PlanError.missingPattern(frame.key) :
                PlanError.unsatisfiedBaseResource(frame.key);
            session.ledger.recordUnsatisfiedInventoryDemand(frame.key, frame.remaining);
            popFailure(session, error);
            return;
        }
        frame.firstError = null;
        frame.errorSummary = null;
        frame.candidateIndex = 0;
        frame.stage = Stage.SELECT_PATTERN;
    }

    private void loadAvailableAmount(PlanningSession session, IStackKey key) {
        if (session.cachedAvailable.containsKey(key)) {
            return;
        }
        var available = inventory.amountOf(key);
        session.cachedAvailable.put(key, available);
        session.ledger.observeInventory(key, available);
    }

    private void runSelectPatternStage(PlanningSession session, SearchFrame frame) {
        if (frame.candidateIndex >= frame.candidates.size()) {
            var error = frame.firstError == null ? PlanError.unsatisfiedBaseResource(frame.key) : frame.firstError;
            var summary = frame.errorSummary == null ? session.ledger.summary() : frame.errorSummary;
            popFailure(session, error, summary);
            return;
        }
        frame.ledgerSnapshot = session.ledger.copy();
        frame.stepCountSnapshot = session.steps.size();
        frame.stepIdSnapshot = session.nextStepId;
        frame.inputIndex = 0;
        frame.childError = null;
        frame.childErrorSummary = null;
        var pattern = frame.candidates.get(frame.candidateIndex);
        frame.runs = requiredRuns(pattern, frame.key, frame.remaining);
        for (var output : pattern.outputs()) {
            session.ledger.recordCraftedAmount(output.key(), output.amount() * frame.runs);
        }
        frame.stage = Stage.REDUCE_INPUTS;
    }

    private void runReduceInputsStage(PlanningSession session, SearchFrame frame) {
        if (frame.childError != null) {
            if (frame.firstError == null) {
                frame.firstError = frame.childError;
                frame.errorSummary = frame.childErrorSummary == null ?
                    session.ledger.summary() :
                    frame.childErrorSummary;
            }
            rollbackCandidate(session, frame);
            frame.childError = null;
            frame.childErrorSummary = null;
            frame.candidateIndex++;
            frame.stage = Stage.SELECT_PATTERN;
            return;
        }
        var pattern = frame.candidates.get(frame.candidateIndex);
        if (frame.inputIndex >= pattern.inputs().size()) {
            frame.stage = Stage.APPLY_PATTERN;
            return;
        }
        var input = pattern.inputs().get(frame.inputIndex);
        frame.inputIndex++;
        session.searchStack.add(new SearchFrame(input.key(), input.amount() * frame.runs, false));
    }

    private void runApplyPatternStage(PlanningSession session, SearchFrame frame) {
        var pattern = frame.candidates.get(frame.candidateIndex);
        for (var output : pattern.outputs()) {
            session.ledger.addCraftedStock(output.key(), output.amount() * frame.runs);
        }
        var fulfilled = frame.rootDemand ?
            session.ledger.consumeCrafted(frame.key, frame.remaining) :
            session.ledger.consume(frame.key, frame.remaining);
        session.steps.add(new CraftStep(
            "step-" + session.nextStepId++,
            pattern,
            frame.runs));
        frame.remaining -= fulfilled;
        if (frame.remaining <= 0L) {
            popSuccess(session);
            return;
        }
        frame.stage = Stage.SELECT_PATTERN;
    }

    private void rollbackCandidate(PlanningSession session, SearchFrame frame) {
        session.ledger.reset(frame.ledgerSnapshot);
        while (session.steps.size() > frame.stepCountSnapshot) {
            session.steps.removeLast();
        }
        session.nextStepId = frame.stepIdSnapshot;
    }

    private SearchFrame peekFrame(PlanningSession session) {
        return session.searchStack.getLast();
    }

    private void popSuccess(PlanningSession session) {
        session.searchStack.removeLast();
        if (session.searchStack.isEmpty()) {
            session.nextTargetIndex++;
        }
    }

    private void popFailure(PlanningSession session, PlanError error) {
        popFailure(session, error, session.ledger.summary());
    }

    private void popFailure(PlanningSession session, PlanError error, PlanSummary summary) {
        session.searchStack.removeLast();
        if (session.searchStack.isEmpty()) {
            session.result = PlanResult.failed(error, summary);
            return;
        }
        var parent = peekFrame(session);
        parent.childError = error;
        parent.childErrorSummary = summary;
    }

    @Nullable
    private static PlanError detectCycle(List<SearchFrame> stack) {
        var current = stack.getLast();
        for (var i = 0; i < stack.size() - 1; i++) {
            if (stack.get(i).key.equals(current.key)) {
                return PlanError.cycleDetected(current.key);
            }
        }
        return null;
    }

    private List<CraftPattern> choosePatterns(IStackKey key) {
        return patterns.findPatternsProducing(key).stream()
            .sorted(Comparator.comparing(CraftPattern::patternUuid))
            .toList();
    }

    private CraftPlan buildPlan(PlanningSession session) {
        var summary = session.ledger.summary();
        return new CraftPlan(session.steps, summary, calculateMemoryUsage(session.steps, summary));
    }

    private static long requiredRuns(CraftPattern pattern, IStackKey key, long remainingDemand) {
        var producedPerRun = 0L;
        for (var output : pattern.outputs()) {
            if (output.key().equals(key)) {
                producedPerRun += output.amount();
            }
        }
        return divideCeil(remainingDemand, producedPerRun);
    }

    private long calculateMemoryUsage(List<CraftStep> steps, PlanSummary summary) {
        var ret = memoryConfig.bytesPerStep() * steps.size();
        for (var entry : summary.entries().entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var amount = value.consumedFromInventory() + value.craftedAmount();
            if (key.type() == PortType.ITEM) {
                ret += memoryConfig.bytesPerItemType() + memoryConfig.bytesPerItem() * amount;
            } else if (key.type() == PortType.FLUID) {
                ret += memoryConfig.bytesPerFluidType() + memoryConfig.bytesPerFluid() * amount;
            }
        }
        return ret;
    }

    private static long divideCeil(long numerator, long denominator) {
        if (denominator <= 0L || numerator <= 0L) {
            return 0L;
        }
        return (numerator + denominator - 1L) / denominator;
    }

    private static final class PlanningSession {
        private final List<CraftAmount> targets;
        private final PlannerLedger ledger;
        private final Map<IStackKey, Long> cachedAvailable;
        private final List<CraftStep> steps;
        private final List<SearchFrame> searchStack;
        private int nextTargetIndex;
        private long nextStepId;
        @Nullable
        private PlanResult result;

        private PlanningSession(List<CraftAmount> targets) {
            this.targets = List.copyOf(targets);
            this.ledger = new PlannerLedger();
            this.cachedAvailable = new LinkedHashMap<>();
            this.steps = new ArrayList<>();
            this.searchStack = new ArrayList<>();
            this.nextTargetIndex = 0;
            this.nextStepId = 1L;
            this.result = null;
        }
    }

    private static final class SearchFrame {
        private final IStackKey key;
        private final long demand;
        private final boolean rootDemand;
        private long remaining;
        private List<CraftPattern> candidates;
        private int candidateIndex;
        private int inputIndex;
        private long runs;
        private PlannerLedger ledgerSnapshot;
        private int stepCountSnapshot;
        private long stepIdSnapshot;
        @Nullable
        private PlanError firstError;
        @Nullable
        private PlanSummary errorSummary;
        @Nullable
        private PlanError childError;
        @Nullable
        private PlanSummary childErrorSummary;
        private Stage stage;

        private SearchFrame(IStackKey key, long demand, boolean rootDemand) {
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
            this.errorSummary = null;
            this.childError = null;
            this.childErrorSummary = null;
            this.stage = Stage.START;
        }
    }

    private enum Stage {
        START,
        SELECT_PATTERN,
        REDUCE_INPUTS,
        APPLY_PATTERN
    }
}
