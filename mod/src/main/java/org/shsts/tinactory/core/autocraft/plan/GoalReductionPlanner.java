package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.api.IIncrementalCraftPlanner;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.api.PlanningState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GoalReductionPlanner implements IIncrementalCraftPlanner {
    private final IPatternRepository patterns;
    private final IInventoryView inventory;

    public GoalReductionPlanner(IPatternRepository patterns, IInventoryView inventory) {
        this.patterns = patterns;
        this.inventory = inventory;
    }

    @Override
    public PlannerSnapshot plan(List<CraftAmount> targets) {
        var session = startSession(targets);
        while (true) {
            var snapshot = resume(session, Integer.MAX_VALUE);
            if (snapshot.state() != PlanningState.RUNNING) {
                return snapshot;
            }
        }
    }

    @Override
    public PlannerSession startSession(List<CraftAmount> targets) {
        return new PlannerSession(targets);
    }

    @Override
    public PlannerSnapshot resume(PlannerSession session, int stepBudget) {
        if (session.result != null) {
            return session.result;
        }
        if (stepBudget <= 0) {
            return PlannerSnapshot.running();
        }

        var budget = stepBudget;
        while (budget > 0 && session.result == null) {
            if (session.searchStack.isEmpty()) {
                if (session.nextTargetIndex >= session.targets.size()) {
                    session.result = PlannerSnapshot.completed(buildPlan(session));
                    return session.result;
                }
                var target = session.targets.get(session.nextTargetIndex);
                session.searchStack.add(new PlannerSession.SearchFrame(target.key(), target.amount(), true));
            }
            processOneSearchStep(session);
            budget--;
        }
        if (session.result != null) {
            return session.result;
        }
        if (session.nextTargetIndex >= session.targets.size() && session.searchStack.isEmpty()) {
            session.result = PlannerSnapshot.completed(buildPlan(session));
            return session.result;
        }
        return PlannerSnapshot.running();
    }

    private void processOneSearchStep(PlannerSession session) {
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

    private void runStartStage(PlannerSession session, PlannerSession.SearchFrame frame) {
        loadAvailableAmount(session, frame.key);
        frame.remaining = frame.demand - session.ledger.consume(frame.key, frame.demand);
        if (frame.remaining <= 0L) {
            popSuccess(session);
            return;
        }
        var cycleError = detectCycle(session.searchStack);
        if (cycleError != null) {
            popFailure(session, cycleError);
            return;
        }
        frame.candidates = choosePatterns(frame.key);
        if (frame.candidates.isEmpty()) {
            var error = frame.rootDemand ?
                PlanError.missingPattern(frame.key) :
                PlanError.unsatisfiedBaseResource(frame.key);
            popFailure(session, error);
            return;
        }
        frame.firstError = null;
        frame.candidateIndex = 0;
        frame.stage = PlannerSession.Stage.SELECT_PATTERN;
    }

    private void loadAvailableAmount(PlannerSession session, IStackKey key) {
        if (session.cachedAvailable.containsKey(key)) {
            return;
        }
        var available = inventory.amountOf(key);
        session.cachedAvailable.put(key, available);
        session.ledger.add(key, available);
    }

    private void runSelectPatternStage(PlannerSession session, PlannerSession.SearchFrame frame) {
        if (frame.candidateIndex >= frame.candidates.size()) {
            var error = frame.firstError == null ? PlanError.unsatisfiedBaseResource(frame.key) : frame.firstError;
            popFailure(session, error);
            return;
        }
        frame.ledgerSnapshot = session.ledger.copy();
        frame.stepCountSnapshot = session.steps.size();
        frame.stepIdSnapshot = session.nextStepId;
        frame.runs = 1L;
        frame.inputIndex = 0;
        frame.childError = null;
        frame.stage = PlannerSession.Stage.REDUCE_INPUTS;
    }

    private void runReduceInputsStage(PlannerSession session, PlannerSession.SearchFrame frame) {
        if (frame.childError != null) {
            if (frame.firstError == null) {
                frame.firstError = frame.childError;
            }
            rollbackCandidate(session, frame);
            frame.childError = null;
            frame.candidateIndex++;
            frame.stage = PlannerSession.Stage.SELECT_PATTERN;
            return;
        }
        var pattern = frame.candidates.get(frame.candidateIndex);
        if (frame.inputIndex >= pattern.inputs().size()) {
            frame.stage = PlannerSession.Stage.APPLY_PATTERN;
            return;
        }
        var input = pattern.inputs().get(frame.inputIndex);
        frame.inputIndex++;
        session.searchStack.add(new PlannerSession.SearchFrame(input.key(), input.amount() * frame.runs, false));
    }

    private void runApplyPatternStage(PlannerSession session, PlannerSession.SearchFrame frame) {
        var pattern = frame.candidates.get(frame.candidateIndex);
        for (var output : pattern.outputs()) {
            session.ledger.add(output.key(), output.amount() * frame.runs);
        }
        var fulfilled = session.ledger.consume(frame.key, frame.remaining);
        session.steps.add(new CraftStep(
            "step-" + session.nextStepId++,
            pattern,
            frame.runs,
            List.of(new CraftAmount(frame.key, fulfilled))));
        frame.remaining -= fulfilled;
        if (frame.remaining <= 0L) {
            popSuccess(session);
            return;
        }
        frame.stage = PlannerSession.Stage.SELECT_PATTERN;
    }

    private void rollbackCandidate(PlannerSession session, PlannerSession.SearchFrame frame) {
        session.ledger.reset(frame.ledgerSnapshot);
        while (session.steps.size() > frame.stepCountSnapshot) {
            session.steps.remove(session.steps.size() - 1);
        }
        session.nextStepId = frame.stepIdSnapshot;
    }

    private PlannerSession.SearchFrame peekFrame(PlannerSession session) {
        return session.searchStack.get(session.searchStack.size() - 1);
    }

    private void popSuccess(PlannerSession session) {
        session.searchStack.remove(session.searchStack.size() - 1);
        if (session.searchStack.isEmpty()) {
            session.nextTargetIndex++;
        }
    }

    private void popFailure(PlannerSession session, PlanError error) {
        session.searchStack.remove(session.searchStack.size() - 1);
        if (session.searchStack.isEmpty()) {
            session.result = PlannerSnapshot.failed(error);
            return;
        }
        var parent = peekFrame(session);
        parent.childError = error;
    }

    @Nullable
    private static PlanError detectCycle(List<PlannerSession.SearchFrame> stack) {
        var current = stack.get(stack.size() - 1);
        for (var i = 0; i < stack.size() - 1; i++) {
            if (stack.get(i).key.equals(current.key)) {
                var cyclePath = new ArrayList<IStackKey>();
                for (var j = i; j < stack.size(); j++) {
                    cyclePath.add(stack.get(j).key);
                }
                return PlanError.cycleDetected(cyclePath);
            }
        }
        return null;
    }

    private List<CraftPattern> choosePatterns(IStackKey key) {
        return patterns.findPatternsProducing(key).stream()
            .sorted(Comparator.comparing(CraftPattern::patternId))
            .toList();
    }

    private static CraftPlan buildPlan(PlannerSession session) {
        return new CraftPlan(classifyStepOutputRoles(session.steps, session.targets));
    }

    private static List<CraftStep> classifyStepOutputRoles(List<CraftStep> steps, List<CraftAmount> targets) {
        var remainingIntermediateDemand = new LinkedHashMap<IStackKey, Long>();
        var remainingFinalDemand = new LinkedHashMap<IStackKey, Long>();
        for (var target : targets) {
            addDemand(remainingFinalDemand, target.key(), target.amount());
        }
        var out = new ArrayList<CraftStep>(steps.size());
        for (var i = steps.size() - 1; i >= 0; i--) {
            var step = steps.get(i);
            var stepOutputs = aggregateOutputs(step.pattern().outputs(), step.runs());
            var requiredIntermediateOutputs = new ArrayList<CraftAmount>();
            var requiredFinalOutputs = new ArrayList<CraftAmount>();
            for (var output : stepOutputs.entrySet()) {
                var intermediateRequired = consumeDemand(
                    remainingIntermediateDemand,
                    output.getKey(),
                    output.getValue());
                var finalRequired = consumeDemand(
                    remainingFinalDemand,
                    output.getKey(),
                    output.getValue() - intermediateRequired);
                if (intermediateRequired > 0L) {
                    requiredIntermediateOutputs.add(new CraftAmount(output.getKey(), intermediateRequired));
                }
                if (finalRequired > 0L) {
                    requiredFinalOutputs.add(new CraftAmount(output.getKey(), finalRequired));
                }
            }
            out.add(0, new CraftStep(
                step.stepId(),
                step.pattern(),
                step.runs(),
                requiredIntermediateOutputs,
                requiredFinalOutputs));
            for (var input : step.pattern().inputs()) {
                addDemand(remainingIntermediateDemand, input.key(), input.amount() * step.runs());
            }
        }
        return out;
    }

    private static Map<IStackKey, Long> aggregateOutputs(List<CraftAmount> outputs, long runs) {
        var aggregated = new LinkedHashMap<IStackKey, Long>();
        for (var output : outputs) {
            addDemand(aggregated, output.key(), output.amount() * runs);
        }
        return aggregated;
    }

    private static void addDemand(Map<IStackKey, Long> demandMap, IStackKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        demandMap.put(key, demandMap.getOrDefault(key, 0L) + amount);
    }

    private static long consumeDemand(Map<IStackKey, Long> demandMap, IStackKey key, long availableAmount) {
        if (availableAmount <= 0L) {
            return 0L;
        }
        var demand = demandMap.getOrDefault(key, 0L);
        var consumed = Math.min(demand, availableAmount);
        if (consumed <= 0L) {
            return 0L;
        }
        if (consumed == demand) {
            demandMap.remove(key);
        } else {
            demandMap.put(key, demand - consumed);
        }
        return consumed;
    }
}
