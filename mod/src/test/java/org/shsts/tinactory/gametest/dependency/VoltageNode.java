package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.core.electric.Voltage;

import java.util.Set;

record VoltageNode(Voltage voltage) implements IDependencyNode {
    @Override
    public String type() {
        return "voltage";
    }

    @Override
    public String id() {
        return voltage.id;
    }

    @Override
    public boolean isSatisfied(IDependencySolver checker) {
        return checker.maxVoltageRank() >= voltage.rank;
    }

    @Override
    public boolean reach(IDependencySolver checker, DependencyMethod method) {
        return checker.reachVoltage(voltage, method);
    }

    @Override
    public void addWaiter(IDependencySolver checker, DependencyMethod method) {
        checker.addVoltageWaiter(this, method);
    }

    @Override
    public Set<DependencyMethod> releaseWaiters(IDependencySolver checker) {
        return checker.releaseVoltageWaiters(this);
    }
}
