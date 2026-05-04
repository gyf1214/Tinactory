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
