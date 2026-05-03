package org.shsts.tinactory.gametest.dependency;

import java.util.Set;

interface IDependencyNode extends Comparable<IDependencyNode> {
    String type();

    String id();

    boolean isSatisfied(IDependencySolver checker);

    boolean reach(IDependencySolver checker, DependencyMethod method);

    void addWaiter(IDependencySolver checker, DependencyMethod method);

    Set<DependencyMethod> releaseWaiters(IDependencySolver checker);

    @Override
    default int compareTo(IDependencyNode other) {
        var typeCompare = type().compareTo(other.type());
        if (typeCompare != 0) {
            return typeCompare;
        }
        return id().compareTo(other.id());
    }

    default String displayId() {
        return type() + ":" + id();
    }
}
