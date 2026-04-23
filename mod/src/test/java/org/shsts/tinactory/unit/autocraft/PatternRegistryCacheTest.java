package org.shsts.tinactory.unit.autocraft;

import org.shsts.tinactory.unit.fixture.TestStackKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineRequirement;
import org.shsts.tinactory.core.autocraft.pattern.PatternRegistryCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternRegistryCacheTest {
    @Test
    void addCellPortShouldRejectDuplicatePatternIdsAcrossCells() {
        var repo = new PatternRegistryCache();
        var machineA = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var machineB = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertTrue(repo.addCellPort(machineA, 1, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_plate")))));
        assertFalse(repo.addCellPort(machineB, 1, 0,
            new TestCellPort(List.of(pattern("tinactory:p1", "tinactory:iron_gear")))));
    }

    @Test
    void findPatternsProducingShouldReturnPatternIdSortedList() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var out = TestStackKey.item("tinactory:iron_plate", "");

        var p2 = pattern("tinactory:p2", "tinactory:iron_plate");
        var p1 = pattern("tinactory:p1", "tinactory:iron_plate");
        assertTrue(repo.addCellPort(machine, 3, 0, new TestCellPort(List.of(p2, p1))));

        assertEquals(List.of("tinactory:p1", "tinactory:p2"),
            repo.findPatternsProducing(out).stream().map(CraftPattern::patternId).toList());
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

        assertTrue(highPort.contains("tinactory:p-new"));
        assertFalse(lowPort.contains("tinactory:p-new"));
    }

    @Test
    void removePatternShouldWriteThroughAndDropIndexes() {
        var repo = new PatternRegistryCache();
        var machine = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var existing = pattern("tinactory:to_remove", "tinactory:iron_plate");
        var port = new TestCellPort(List.of(existing));

        assertTrue(repo.addCellPort(machine, 1, 0, port));
        assertTrue(repo.removePattern(existing.patternId()));
        assertFalse(repo.containsPatternId(existing.patternId()));
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
        assertTrue(repo.containsPatternId(original.patternId()));
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
        assertFalse(repo.containsPatternId("tinactory:p1"));
        assertFalse(repo.containsPatternId("tinactory:p2"));
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

    private static CraftPattern pattern(String patternId, String outputId) {
        return new CraftPattern(
            patternId,
            List.of(new CraftAmount(TestStackKey.item("tinactory:iron_ingot", ""), 1)),
            List.of(new CraftAmount(TestStackKey.item(outputId, ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static final class TestCellPort implements IPatternCellPort {
        private final Map<String, CraftPattern> patterns = new HashMap<>();
        private final Predicate<CraftPattern> insertRule;

        private TestCellPort(List<CraftPattern> initial) {
            this(initial, $ -> true);
        }

        private TestCellPort(List<CraftPattern> initial, Predicate<CraftPattern> insertRule) {
            this.insertRule = insertRule;
            for (var pattern : initial) {
                patterns.put(pattern.patternId(), pattern);
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
            patterns.put(pattern.patternId(), pattern);
            return true;
        }

        @Override
        public boolean remove(String patternId) {
            return patterns.remove(patternId) != null;
        }

        @Override
        public int bytesCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int bytesUsed() {
            return patterns.size() * 256;
        }

        private boolean contains(String patternId) {
            return patterns.containsKey(patternId);
        }
    }
}
