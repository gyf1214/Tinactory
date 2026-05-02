package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.Set;

record StackNode(IStackKey key) implements IDependencyNode {
    @Override
    public String type() {
        return "stack";
    }

    @Override
    public String id() {
        return key.type().name().toLowerCase() + ":" + key;
    }

    @Override
    public boolean isSatisfied(IDependencyChecker checker) {
        return checker.isExactReached(this);
    }

    @Override
    public boolean reach(IDependencyChecker checker, DependencyMethod method) {
        return checker.reachExact(this, method);
    }

    @Override
    public void addWaiter(IDependencyChecker checker, DependencyMethod method) {
        checker.addExactWaiter(this, method);
    }

    @Override
    public Set<DependencyMethod> releaseWaiters(IDependencyChecker checker) {
        return checker.releaseExactWaiters(this);
    }
}
