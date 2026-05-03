package org.shsts.tinactory.gametest.dependency;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.Set;

record TagNode(PortType portType, ResourceLocation tagId) implements IDependencyNode {
    @Override
    public String type() {
        return "tag";
    }

    @Override
    public String id() {
        return portType.name().toLowerCase() + ":" + tagId;
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
