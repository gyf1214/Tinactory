package org.shsts.tinactory.gametest.dependency;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

record DependencyMethod(String id, Set<IDependencyNode> requirements, Set<IDependencyNode> outputs, String source)
    implements Comparable<DependencyMethod> {
    DependencyMethod {
        requirements = normalized(requirements);
        outputs = normalized(outputs);
    }

    DependencyMethod(String id, Collection<IDependencyNode> requirements, Collection<IDependencyNode> outputs,
        String source) {
        this(id, Set.copyOf(requirements), Set.copyOf(outputs), source);
    }

    private static Set<IDependencyNode> normalized(Collection<IDependencyNode> nodes) {
        return new LinkedHashSet<>(new TreeSet<>(nodes));
    }

    @Override
    public int compareTo(DependencyMethod other) {
        return id.compareTo(other.id);
    }
}
