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
    public boolean isSatisfied(IDependencyChecker checker) {
        return checker.maxVoltage().rank >= voltage.rank;
    }

    @Override
    public boolean reach(IDependencyChecker checker, DependencyMethod method) {
        return checker.reachVoltage(voltage, method);
    }

    @Override
    public void addWaiter(IDependencyChecker checker, DependencyMethod method) {
        checker.addVoltageWaiter(this, method);
    }

    @Override
    public Set<DependencyMethod> releaseWaiters(IDependencyChecker checker) {
        return checker.releaseVoltageWaiters(this);
    }
}
