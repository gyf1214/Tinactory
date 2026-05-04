package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.core.electric.Voltage;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class DependencySolver implements IDependencySolver {
    private final Collection<DependencyMethod> methods;
    private final Queue<DependencyMethod> readyMethods = new PriorityQueue<>();
    private final Queue<IDependencyNode> newlyReached = new PriorityQueue<>();
    private final Set<DependencyMethod> appliedMethods = new HashSet<>();
    private final Map<DependencyMethod, Integer> remainingRequirements = new HashMap<>();
    private final Map<IDependencyNode, Set<DependencyMethod>> waitingByExactNode = new HashMap<>();
    private final Map<String, NavigableMap<Double, Set<DependencyMethod>>> waitingByNumericNode = new HashMap<>();
    private final NavigableMap<Integer, Set<DependencyMethod>> waitingByVoltage = new TreeMap<>();
    private final Set<IDependencyNode> reachedExactNodes = new TreeSet<>();
    private final Map<String, Double> maxNumericNodes = new HashMap<>();
    private int maxReachedVoltageRank = -1;

    DependencySolver(Collection<DependencyMethod> methods) {
        this.methods = methods;
    }

    public void solve(Collection<IDependencyNode> startNodes) {
        reset();
        for (var node : startNodes) {
            node.reach(this, bootstrapMethod());
        }
        initializeMethods();
        while (!readyMethods.isEmpty() || !newlyReached.isEmpty()) {
            applyReadyMethods();
            releaseReachedNodes();
        }
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
    public int maxVoltageRank() {
        return maxReachedVoltageRank;
    }

    @Override
    public boolean reachVoltage(Voltage voltage, DependencyMethod method) {
        if (maxReachedVoltageRank >= voltage.rank) {
            return false;
        }
        maxReachedVoltageRank = voltage.rank;
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

    private void reset() {
        readyMethods.clear();
        newlyReached.clear();
        appliedMethods.clear();
        remainingRequirements.clear();
        waitingByExactNode.clear();
        waitingByNumericNode.clear();
        waitingByVoltage.clear();
        reachedExactNodes.clear();
        maxNumericNodes.clear();
        maxReachedVoltageRank = -1;
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

    private static DependencyMethod bootstrapMethod() {
        return new DependencyMethod("bootstrap/start", Set.of(), Set.of(), "start list bootstrap");
    }
}
