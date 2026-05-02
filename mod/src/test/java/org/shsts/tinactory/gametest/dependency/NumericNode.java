package org.shsts.tinactory.gametest.dependency;

import java.util.Set;

record NumericNode(String key, double value) implements IDependencyNode {
    @Override
    public String type() {
        return "numeric";
    }

    @Override
    public String id() {
        return key + ">=" + value;
    }

    @Override
    public boolean isSatisfied(IDependencyChecker checker) {
        return checker.maxNumeric(key) >= value;
    }

    @Override
    public boolean reach(IDependencyChecker checker, DependencyMethod method) {
        return checker.reachNumeric(key, value, method);
    }

    @Override
    public void addWaiter(IDependencyChecker checker, DependencyMethod method) {
        checker.addNumericWaiter(this, method);
    }

    @Override
    public Set<DependencyMethod> releaseWaiters(IDependencyChecker checker) {
        return checker.releaseNumericWaiters(this);
    }
}
