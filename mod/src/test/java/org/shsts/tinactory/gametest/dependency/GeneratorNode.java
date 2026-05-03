package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.core.electric.Voltage;

import java.util.Set;

record GeneratorNode(Voltage voltage) implements IDependencyNode {
    @Override
    public String type() {
        return "generator";
    }

    @Override
    public String id() {
        return voltage.id;
    }

    @Override
    public boolean isSatisfied(IDependencySolver checker) {
        return checker.isExactReached(this);
    }

    @Override
    public boolean reach(IDependencySolver checker, DependencyMethod method) {
        return checker.reachExact(this, method);
    }

    @Override
    public void addWaiter(IDependencySolver checker, DependencyMethod method) {
        checker.addExactWaiter(this, method);
    }

    @Override
    public Set<DependencyMethod> releaseWaiters(IDependencySolver checker) {
        return checker.releaseExactWaiters(this);
    }
}
