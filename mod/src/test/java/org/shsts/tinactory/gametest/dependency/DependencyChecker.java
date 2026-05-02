package org.shsts.tinactory.gametest.dependency;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class DependencyChecker implements IDependencyChecker {
    private final List<DependencyMethod> methods = new ArrayList<>();
    private final Queue<DependencyMethod> readyMethods = new PriorityQueue<>();
    private final Queue<IDependencyNode> newlyReached = new PriorityQueue<>();
    private final Set<DependencyMethod> appliedMethods = new HashSet<>();
    private final Map<DependencyMethod, Integer> remainingRequirements = new HashMap<>();
    private final Map<IDependencyNode, Set<DependencyMethod>> waitingByExactNode = new HashMap<>();
    private final Map<String, NavigableMap<Double, Set<DependencyMethod>>> waitingByNumericNode = new HashMap<>();
    private final NavigableMap<Integer, Set<DependencyMethod>> waitingByVoltage = new TreeMap<>();
    private final Set<IDependencyNode> reachedExactNodes = new TreeSet<>();
    private final Map<IDependencyNode, DependencyMethod> firstReachingMethods = new HashMap<>();
    private final Map<String, Double> maxNumericNodes = new HashMap<>();
    private final Map<String, DependencyMethod> firstReachingNumericMethods = new HashMap<>();
    private final Map<Voltage, DependencyMethod> firstReachingVoltageMethods = new HashMap<>();
    private Voltage maxReachedVoltage = Voltage.PRIMITIVE;

    public void addMethod(DependencyMethod method) {
        methods.add(method);
    }

    public boolean isReached(IDependencyNode node) {
        return node.isSatisfied(this);
    }

    public void solve(Collection<IDependencyNode> startNodes) {
        resetSolver();
        for (var node : startNodes) {
            node.reach(this, bootstrapMethod());
        }
        initializeMethods();
        while (!readyMethods.isEmpty() || !newlyReached.isEmpty()) {
            applyReadyMethods();
            releaseReachedNodes();
        }
    }

    public static void runSelfCheck() {
        var checker = new DependencyChecker();
        var seed = technology("seed");
        var exact = technology("exact");
        var numeric = technology("numeric");
        var voltage = technology("voltage");
        var duplicate = technology("duplicate");
        checker.addMethod(new DependencyMethod(
            "self/exact", List.of(seed), List.of(exact), "self check exact"));
        checker.addMethod(new DependencyMethod(
            "self/numeric", List.of(new NumericNode("temperature", 2d)), List.of(numeric), "self check numeric"));
        checker.addMethod(new DependencyMethod(
            "self/voltage", List.of(new VoltageNode(Voltage.MV)), List.of(voltage), "self check voltage"));
        checker.addMethod(new DependencyMethod(
            "self/duplicate", List.of(exact, exact), List.of(duplicate), "self check duplicate"));
        checker.solve(List.of(seed, new NumericNode("temperature", 3d), new VoltageNode(Voltage.HV)));
        checker.requireReached(exact);
        checker.requireReached(numeric);
        checker.requireReached(voltage);
        checker.requireReached(duplicate);
    }

    @Override
    public boolean isExactReached(IDependencyNode node) {
        return reachedExactNodes.contains(node);
    }

    @Override
    public boolean reachExact(IDependencyNode node, DependencyMethod method) {
        if (!reachedExactNodes.add(node)) {
            return false;
        }
        firstReachingMethods.put(node, method);
        newlyReached.add(node);
        return true;
    }

    @Override
    public void addExactWaiter(IDependencyNode node, DependencyMethod method) {
        waitingByExactNode.computeIfAbsent(node, $ -> new TreeSet<>()).add(method);
    }

    @Override
    public Set<DependencyMethod> releaseExactWaiters(IDependencyNode node) {
        return waitingByExactNode.getOrDefault(node, Set.of());
    }

    @Override
    public double maxNumeric(String key) {
        return maxNumericNodes.getOrDefault(key, Double.NEGATIVE_INFINITY);
    }

    @Override
    public boolean reachNumeric(String key, double value, DependencyMethod method) {
        var oldValue = maxNumeric(key);
        if (oldValue >= value) {
            return false;
        }
        maxNumericNodes.put(key, value);
        firstReachingNumericMethods.put(key, method);
        newlyReached.add(new NumericNode(key, value));
        return true;
    }

    @Override
    public void addNumericWaiter(NumericNode node, DependencyMethod method) {
        waitingByNumericNode.computeIfAbsent(node.key(), $ -> new TreeMap<>())
            .computeIfAbsent(node.value(), $ -> new TreeSet<>())
            .add(method);
    }

    @Override
    public Set<DependencyMethod> releaseNumericWaiters(NumericNode node) {
        var methodsByValue = waitingByNumericNode.get(node.key());
        if (methodsByValue == null) {
            return Set.of();
        }
        var released = new TreeSet<DependencyMethod>();
        var reachedValues = new TreeMap<>(methodsByValue.headMap(node.value(), true));
        for (var methodSet : reachedValues.values()) {
            released.addAll(methodSet);
        }
        reachedValues.keySet().forEach(methodsByValue::remove);
        return released;
    }

    @Override
    public Voltage maxVoltage() {
        return maxReachedVoltage;
    }

    @Override
    public boolean reachVoltage(Voltage voltage, DependencyMethod method) {
        if (maxReachedVoltage.rank >= voltage.rank) {
            return false;
        }
        maxReachedVoltage = voltage;
        firstReachingVoltageMethods.put(voltage, method);
        newlyReached.add(new VoltageNode(voltage));
        return true;
    }

    @Override
    public void addVoltageWaiter(VoltageNode node, DependencyMethod method) {
        waitingByVoltage.computeIfAbsent(node.voltage().rank, $ -> new TreeSet<>()).add(method);
    }

    @Override
    public Set<DependencyMethod> releaseVoltageWaiters(VoltageNode node) {
        var released = new TreeSet<DependencyMethod>();
        var reachedRanks = new TreeMap<>(waitingByVoltage.headMap(node.voltage().rank, true));
        for (var methodSet : reachedRanks.values()) {
            released.addAll(methodSet);
        }
        reachedRanks.keySet().forEach(waitingByVoltage::remove);
        return released;
    }

    private void resetSolver() {
        readyMethods.clear();
        newlyReached.clear();
        appliedMethods.clear();
        remainingRequirements.clear();
        waitingByExactNode.clear();
        waitingByNumericNode.clear();
        waitingByVoltage.clear();
        reachedExactNodes.clear();
        firstReachingMethods.clear();
        maxNumericNodes.clear();
        firstReachingNumericMethods.clear();
        firstReachingVoltageMethods.clear();
        maxReachedVoltage = Voltage.PRIMITIVE;
    }

    private void initializeMethods() {
        for (var method : new TreeSet<>(methods)) {
            var remaining = 0;
            for (var requirement : method.requirements()) {
                if (!requirement.isSatisfied(this)) {
                    remaining++;
                    requirement.addWaiter(this, method);
                }
            }
            remainingRequirements.put(method, remaining);
            if (remaining == 0) {
                readyMethods.add(method);
            }
        }
    }

    private void applyReadyMethods() {
        while (!readyMethods.isEmpty()) {
            var method = readyMethods.remove();
            if (!appliedMethods.add(method)) {
                continue;
            }
            for (var output : method.outputs()) {
                output.reach(this, method);
            }
        }
    }

    private void releaseReachedNodes() {
        while (!newlyReached.isEmpty()) {
            var node = newlyReached.remove();
            for (var method : node.releaseWaiters(this)) {
                var remaining = remainingRequirements.merge(method, -1, Integer::sum);
                if (remaining == 0) {
                    readyMethods.add(method);
                }
            }
        }
    }

    private void requireReached(IDependencyNode node) {
        if (!isReached(node)) {
            throw new AssertionError("Expected dependency node to be reached: " + node.displayId());
        }
    }

    private static DependencyMethod bootstrapMethod() {
        return new DependencyMethod("self/bootstrap", List.of(), List.of(), "self check bootstrap");
    }

    private static TechnologyNode technology(String id) {
        return new TechnologyNode(new ResourceLocation(TinactoryKeys.ID, "self_check/" + id));
    }
}
