package org.shsts.tinactory.gametest.dependency;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

record TechnologyNode(ResourceLocation technologyId) implements IDependencyNode {
    @Override
    public String type() {
        return "technology";
    }

    @Override
    public String id() {
        return technologyId.toString();
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
