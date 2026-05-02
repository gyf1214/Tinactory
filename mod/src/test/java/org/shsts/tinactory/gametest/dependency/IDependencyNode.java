package org.shsts.tinactory.gametest.dependency;

import java.util.Set;

interface IDependencyNode extends Comparable<IDependencyNode> {
    String type();

    String id();

    boolean isSatisfied(IDependencyChecker checker);

    boolean reach(IDependencyChecker checker, DependencyMethod method);

    void addWaiter(IDependencyChecker checker, DependencyMethod method);

    Set<DependencyMethod> releaseWaiters(IDependencyChecker checker);

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
