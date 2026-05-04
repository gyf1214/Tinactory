package org.shsts.tinactory.gametest.dependency;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

record IngredientNode(String kind, ResourceLocation owner, int inputIndex) implements IDependencyNode {
    @Override
    public String type() {
        return "ingredient";
    }

    @Override
    public String id() {
        return kind + "/" + owner + "#" + inputIndex;
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
