package org.shsts.tinactory.gametest.dependency;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.Set;

record MachineNode(ResourceLocation recipeTypeId, Voltage voltage) implements IDependencyNode {
    @Override
    public String type() {
        return "machine";
    }

    @Override
    public String id() {
        return recipeTypeId + "@" + voltage.id;
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
