package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;
import org.shsts.tinactory.unit.fixture.TestAutocraftHelper;
import org.shsts.tinactory.unit.fixture.TestStackKey;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternRegistryCacheTest {
    @Test
    void addCellPortShouldRejectDuplicatePatternUuidsAcrossCells() {
        var repo = new PatternRegistryCache();
        var machineA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var machineB = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertTrue(repo.addCellPort(machineA, 1, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_plate")))));
        assertFalse(repo.addCellPort(machineB, 1, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_gear")))));
    }

    @Test
    void findPatternsProducingShouldReturnUuidSortedList() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var out = TestStackKey.item("tinactory:iron_plate", "");

        var p2 = pattern("tinactory:p2", "tinactory:iron_plate");
        var p1 = pattern("tinactory:p1", "tinactory:iron_plate");
        assertTrue(repo.addCellPort(machine, 3, 0, new TestCellPort(List.of(p2, p1))));

        assertEquals(List.of(uuid("tinactory:p2"), uuid("tinactory:p1")),
            repo.findPatternsProducing(out).stream().map(CraftPattern::patternUuid).toList());
    }

    @Test
    void listPatternsShouldReturnOutputThenUuidSortedImmutableSnapshot() {
        var repo = new PatternRegistryCache();
        var machineA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var machineB = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertTrue(repo.addCellPort(machineA, 1, 0, new TestCellPort(List.of(
            pattern("tinactory:p3", "tinactory:iron_plate"),
            pattern("tinactory:p1", "tinactory:copper_plate")))));
        assertTrue(repo.addCellPort(machineB, 1, 0, new TestCellPort(List.of(
            pattern("tinactory:p2", "tinactory:gold_plate")))));

        var snapshot = repo.listPatterns();

        assertEquals(List.of(uuid("tinactory:p1"), uuid("tinactory:p2"), uuid("tinactory:p3")),
            snapshot.stream().map(CraftPattern::patternUuid).toList());
        assertThrows(UnsupportedOperationException.class,
            () -> snapshot.add(pattern("tinactory:p5", "tinactory:diamond_plate")));
        assertTrue(repo.addPattern(pattern("tinactory:p4", "tinactory:steel_plate")));
        assertEquals(List.of(uuid("tinactory:p1"), uuid("tinactory:p2"), uuid("tinactory:p3")),
            snapshot.stream().map(CraftPattern::patternUuid).toList());
    }

    @Test
    void addPatternShouldWriteThroughToHighestPriorityWritableCell() {
        var repo = new PatternRegistryCache();
        var highMachine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var lowMachine = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var highPort = new TestCellPort(List.of());
        var lowPort = new TestCellPort(List.of());

        assertTrue(repo.addCellPort(lowMachine, 1, 0, lowPort));
        assertTrue(repo.addCellPort(highMachine, 5, 0, highPort));

        var target = pattern("tinactory:p-new", "tinactory:steel_plate");
        assertTrue(repo.addPattern(target));

        assertTrue(highPort.contains(uuid("tinactory:p-new")));
        assertFalse(lowPort.contains(uuid("tinactory:p-new")));
    }

    @Test
    void removePatternShouldWriteThroughAndDropIndexes() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var existing = pattern("tinactory:to_remove", "tinactory:iron_plate");
        var port = new TestCellPort(List.of(existing));

        assertTrue(repo.addCellPort(machine, 1, 0, port));
        assertTrue(repo.removePattern(existing.patternUuid()));
        assertFalse(repo.containsPatternUuid(existing.patternUuid()));
        assertTrue(repo.findPatternsProducing(
            TestStackKey.item("tinactory:iron_plate", "")).isEmpty());
    }

    @Test
    void updatePatternShouldRollbackWhenReinsertFails() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var original = pattern("tinactory:p1", "tinactory:iron_plate");
        var updated = pattern("tinactory:p1", "tinactory:gold_plate");
        var port = new TestCellPort(List.of(original), pattern ->
            pattern.outputs().stream()
                .anyMatch(amount -> amount.key().equals(TestStackKey.item("tinactory:iron_plate", ""))));

        assertTrue(repo.addCellPort(machine, 1, 0, port));
        assertFalse(repo.updatePattern(updated));
        assertTrue(repo.containsPatternUuid(original.patternUuid()));
        assertEquals(List.of(original),
            repo.findPatternsProducing(TestStackKey.item("tinactory:iron_plate", "")));
        assertTrue(repo.findPatternsProducing(TestStackKey.item("tinactory:gold_plate", "")).isEmpty());
    }

    @Test
    void removeCellPortsShouldDropOwnedPatternsAndReturnRemovedCount() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");

        assertTrue(repo.addCellPort(machine, 2, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_plate")))));
        assertTrue(repo.addCellPort(machine, 2, 1,
            new TestCellPort(List.of(pattern("tinactory:p2", "tinactory:iron_gear")))));

        assertEquals(2, repo.removeCellPorts(machine));
        assertFalse(repo.containsPatternUuid(uuid("tinactory:p1")));
        assertFalse(repo.containsPatternUuid(uuid("tinactory:p2")));
    }

    @Test
    void clearShouldResetAllIndexesAndMachinePriority() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");

        assertTrue(repo.addCellPort(machine, 2, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_plate")))));
        repo.clear();

        assertEquals(0, repo.removeCellPorts(machine));
        assertTrue(repo.addCellPort(machine, 5, 0, new TestCellPort(List.of())));
    }

    @Test
    void revisionShouldChangeOnlyWhenContentsChange() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var initialRevision = repo.revision();
        var pattern = pattern("tinactory:p1", "tinactory:iron_plate");

        assertFalse(repo.removePattern(pattern.patternUuid()));
        assertEquals(initialRevision, repo.revision());
        assertTrue(repo.addCellPort(machine, 1, 0, new TestCellPort(List.of(pattern))));
        var addedCellRevision = repo.revision();
        assertTrue(addedCellRevision > initialRevision);

        assertFalse(repo.addCellPort(machine, 1, 0, new TestCellPort(List.of())));
        assertEquals(addedCellRevision, repo.revision());
        assertTrue(repo.updatePattern(pattern("tinactory:p1", "tinactory:copper_plate")));
        var updatedRevision = repo.revision();
        assertTrue(updatedRevision > addedCellRevision);

        assertEquals(1, repo.removeCellPorts(machine));
        assertTrue(repo.revision() > updatedRevision);
    }

    private static CraftPattern pattern(String patternKey, String outputId) {
        return TestAutocraftHelper.pattern(
            uuid(patternKey),
            List.of(new CraftAmount(TestStackKey.item("tinactory:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item(outputId, ""), 1)),
            TestAutocraftHelper.constraints("tinactory:mixer", 0));
    }

    private static UUID uuid(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    private static final class TestCellPort implements IPatternCellPort {
        private final Map<UUID, CraftPattern> patterns = new HashMap<>();
        private final Predicate<CraftPattern> insertRule;

        private TestCellPort(List<CraftPattern> initial) {
            this(initial, $ -> true);
        }

        private TestCellPort(List<CraftPattern> initial, Predicate<CraftPattern> insertRule) {
            this.insertRule = insertRule;
            for (var pattern : initial) {
                patterns.put(pattern.patternUuid(), pattern);
            }
        }

        @Override
        public List<CraftPattern> patterns() {
            return new ArrayList<>(patterns.values());
        }

        @Override
        public boolean insert(CraftPattern pattern) {
            if (!insertRule.test(pattern)) {
                return false;
            }
            patterns.put(pattern.patternUuid(), pattern);
            return true;
        }

        @Override
        public boolean remove(UUID patternUuid) {
            return patterns.remove(patternUuid) != null;
        }

        @Override
        public int bytesCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int bytesUsed() {
            return patterns.size() * 256;
        }

        private boolean contains(UUID patternUuid) {
            return patterns.containsKey(patternUuid);
        }
    }
}
