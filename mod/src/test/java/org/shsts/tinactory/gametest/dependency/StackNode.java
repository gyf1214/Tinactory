package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.Locale;
import java.util.Set;

record StackNode(IStackKey key) implements IDependencyNode {
    @Override
    public String type() {
        return "stack";
    }

    @Override
    public String id() {
        return key.type().name().toLowerCase(Locale.ROOT) + ":" + key;
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
