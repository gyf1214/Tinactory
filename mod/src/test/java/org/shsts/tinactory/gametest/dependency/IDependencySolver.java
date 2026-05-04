package org.shsts.tinactory.gametest.dependency;

import org.shsts.tinactory.core.electric.Voltage;

import java.util.Set;

interface IDependencySolver {
    boolean isExactReached(IDependencyNode node);

    boolean reachExact(IDependencyNode node, DependencyMethod method);

    void addExactWaiter(IDependencyNode node, DependencyMethod method);

    Set<DependencyMethod> releaseExactWaiters(IDependencyNode node);

    double maxNumeric(String key);

    boolean reachNumeric(String key, double value, DependencyMethod method);

    void addNumericWaiter(NumericNode node, DependencyMethod method);

    Set<DependencyMethod> releaseNumericWaiters(NumericNode node);

    int maxVoltageRank();

    boolean reachVoltage(Voltage voltage, DependencyMethod method);

    void addVoltageWaiter(VoltageNode node, DependencyMethod method);

    Set<DependencyMethod> releaseVoltageWaiters(VoltageNode node);
}
