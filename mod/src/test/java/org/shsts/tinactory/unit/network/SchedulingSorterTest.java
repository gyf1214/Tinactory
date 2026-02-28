package org.shsts.tinactory.unit.network;

import net.minecraftforge.registries.ForgeRegistryEntry;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.network.SchedulingSorter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulingSorterTest {
    @Test
    void shouldSortLinearDependencies() {
        var a = new SchedulingFixture("A");
        var b = new SchedulingFixture("B");
        var c = new SchedulingFixture("C");
        a.addBefore(b);
        b.addBefore(c);

        var sorted = SchedulingSorter.sort(List.of(a, b, c));

        assertEquals(List.of(a, b, c), sorted);
    }

    @Test
    void shouldSortMixedBeforeAndAfterDependencies() {
        var a = new SchedulingFixture("A");
        var b = new SchedulingFixture("B");
        var c = new SchedulingFixture("C");
        var d = new SchedulingFixture("D");
        b.addAfter(a);
        c.addBefore(d);
        c.addAfter(b);

        var sorted = SchedulingSorter.sort(List.of(d, c, b, a));

        assertBefore(sorted, a, b);
        assertBefore(sorted, b, c);
        assertBefore(sorted, c, d);
    }

    @Test
    void shouldKeepIndependentNodesInValidTopologicalOrder() {
        var a = new SchedulingFixture("A");
        var b = new SchedulingFixture("B");
        var c = new SchedulingFixture("C");
        var d = new SchedulingFixture("D");
        a.addBefore(b);

        var sorted = SchedulingSorter.sort(List.of(c, b, d, a));

        assertEquals(4, sorted.size());
        assertTrue(sorted.containsAll(List.of(a, b, c, d)));
        assertBefore(sorted, a, b);
    }

    @Test
    void shouldThrowOnCycle() {
        var a = new SchedulingFixture("A");
        var b = new SchedulingFixture("B");
        var c = new SchedulingFixture("C");
        a.addBefore(b);
        b.addBefore(c);
        c.addBefore(a);

        assertThrows(RuntimeException.class, () -> SchedulingSorter.sort(List.of(a, b, c)));
    }

    private static void assertBefore(List<IScheduling> sorted, IScheduling left, IScheduling right) {
        assertTrue(sorted.indexOf(left) < sorted.indexOf(right));
    }

    private static final class SchedulingFixture extends ForgeRegistryEntry<IScheduling>
        implements IScheduling {
        private final String id;
        private final List<Dependency> dependencies = new ArrayList<>();

        private SchedulingFixture(String id) {
            this.id = id;
        }

        private void addBefore(IScheduling right) {
            dependencies.add(new Dependency(this, right));
        }

        private void addAfter(IScheduling left) {
            dependencies.add(new Dependency(left, this));
        }

        @Override
        public void addConditions(BiConsumer<IScheduling, IScheduling> cons) {
            for (var dependency : dependencies) {
                cons.accept(dependency.left(), dependency.right());
            }
        }

        @Override
        public String toString() {
            return id;
        }

        private record Dependency(IScheduling left, IScheduling right) {}
    }
}
