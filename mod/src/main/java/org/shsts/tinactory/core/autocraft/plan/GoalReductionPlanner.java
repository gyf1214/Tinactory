package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GoalReductionPlanner implements ICraftPlanner {
    private final IPatternRepository patterns;

    public GoalReductionPlanner(IPatternRepository patterns) {
        this.patterns = patterns;
    }

    @Override
    public PlanResult plan(List<CraftAmount> targets, List<CraftAmount> available) {
        var steps = new ArrayList<CraftStep>();
        var stepIndex = new StepIndex();
        var ledger = new PlannerLedger();
        for (var resource : available) {
            ledger.add(resource.key(), resource.amount());
        }
        for (var target : targets) {
            var error = reduceTarget(target.key(), target.amount(), ledger, steps, stepIndex, new ArrayList<>(), true);
            if (error != null) {
                return PlanResult.failure(error);
            }
        }
        return PlanResult.success(new CraftPlan(steps));
    }

    private PlanError reduceTarget(
        CraftKey key,
        long amount,
        PlannerLedger ledger,
        List<CraftStep> steps,
        StepIndex stepIndex,
        List<CraftKey> path,
        boolean rootDemand) {
        var remaining = amount - ledger.consume(key, amount);
        if (remaining <= 0L) {
            return null;
        }
        var cycleStart = path.indexOf(key);
        if (cycleStart >= 0) {
            var cyclePath = new ArrayList<>(path.subList(cycleStart, path.size()));
            cyclePath.add(key);
            return PlanError.cycleDetected(cyclePath);
        }

        var candidates = choosePatterns(key);
        if (candidates.isEmpty()) {
            return rootDemand ? PlanError.missingPattern(key) : PlanError.unsatisfiedBaseResource(key);
        }
        path.add(key);
        PlanError firstError = null;
        try {
            for (var pattern : candidates) {
                var ledgerSnapshot = ledger.copy();
                var stepCount = steps.size();
                var stepIndexSnapshot = stepIndex.snapshot();
                var outputPerRun = getProducedAmount(pattern, key);
                var runs = divideCeil(remaining, outputPerRun);
                PlanError error = null;
                for (var input : pattern.inputs()) {
                    error = reduceTarget(input.key(), input.amount() * runs, ledger, steps, stepIndex, path, false);
                    if (error != null) {
                        break;
                    }
                }
                if (error == null) {
                    for (var output : pattern.outputs()) {
                        ledger.add(output.key(), output.amount() * runs);
                    }
                    steps.add(new CraftStep("step-" + stepIndex.next(), pattern, runs));
                    ledger.consume(key, remaining);
                    return null;
                }
                if (firstError == null) {
                    firstError = error;
                }
                ledger.reset(ledgerSnapshot);
                while (steps.size() > stepCount) {
                    steps.remove(steps.size() - 1);
                }
                stepIndex.reset(stepIndexSnapshot);
            }
        } finally {
            path.remove(path.size() - 1);
        }
        return firstError == null ? PlanError.unsatisfiedBaseResource(key) : firstError;
    }

    private List<CraftPattern> choosePatterns(CraftKey key) {
        return patterns.findPatternsProducing(key).stream()
            .sorted(Comparator.comparing(CraftPattern::patternId))
            .toList();
    }

    private static long getProducedAmount(CraftPattern pattern, CraftKey key) {
        for (var output : pattern.outputs()) {
            if (output.key().equals(key)) {
                return output.amount();
            }
        }
        throw new IllegalArgumentException("pattern does not produce target key: " + pattern.patternId());
    }

    private static long divideCeil(long numerator, long denominator) {
        return (numerator + denominator - 1L) / denominator;
    }

    private static final class StepIndex {
        private long value = 1L;

        private long next() {
            return value++;
        }

        private long snapshot() {
            return value;
        }

        private void reset(long snapshot) {
            value = snapshot;
        }
    }
}
