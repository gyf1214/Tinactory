package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternRegistryCache implements IPatternRepository {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Comparator<CraftPattern> DISPLAY_ORDER = Comparator
        .comparing((CraftPattern pattern) -> pattern.outputs().get(0).key())
        .thenComparing(CraftPattern::patternUuid);

    private final Map<CellKey, CellState> cellsByKey = new HashMap<>();
    private final NavigableSet<CellKey> orderedCells = new TreeSet<>();
    private final Map<UUID, PatternEntry> byPatternUuid = new HashMap<>();
    private final Map<IStackKey, List<CraftPattern>> byOutput = new HashMap<>();
    private final Map<UUID, Integer> machinePriority = new HashMap<>();
    private long revision;

    @Override
    public List<CraftPattern> findPatternsProducing(IStackKey key) {
        return List.copyOf(byOutput.getOrDefault(key, List.of()));
    }

    @Override
    public List<IStackKey> listRequestables() {
        return byOutput.keySet().stream().sorted().toList();
    }

    @Override
    public List<CraftPattern> listPatterns() {
        return byPatternUuid.values().stream()
            .map(PatternEntry::pattern)
            .sorted(DISPLAY_ORDER)
            .toList();
    }

    @Override
    public long revision() {
        return revision;
    }

    @Override
    public boolean containsPatternUuid(UUID patternUuid) {
        return byPatternUuid.containsKey(patternUuid);
    }

    @Override
    public boolean addPattern(CraftPattern pattern) {
        if (containsPatternUuid(pattern.patternUuid())) {
            return false;
        }
        if (addPatternToCell(pattern)) {
            revision++;
            return true;
        }
        return false;
    }

    @Override
    public boolean removePattern(UUID patternUuid) {
        var entry = byPatternUuid.get(patternUuid);
        if (entry == null) {
            return false;
        }
        var state = requireCellState(entry.owner());
        if (!state.port().remove(patternUuid)) {
            throw invalidState("pattern %s exists in indexes but owner cell rejected remove", patternUuid);
        }
        removePatternIndexes(entry.owner(), state, entry.pattern());
        revision++;
        return true;
    }

    @Override
    public boolean updatePattern(CraftPattern pattern) {
        var existing = byPatternUuid.get(pattern.patternUuid());
        if (existing == null) {
            return addPattern(pattern);
        }

        var owner = existing.owner();
        var state = requireCellState(owner);
        if (!state.port().remove(existing.pattern().patternUuid())) {
            throw invalidState(
                "pattern %s exists in indexes but owner cell rejected remove",
                existing.pattern().patternUuid());
        }
        removePatternIndexes(owner, state, existing.pattern());

        if (addPatternToCell(pattern)) {
            revision++;
            return true;
        }

        if (!state.port().insert(existing.pattern())) {
            throw invalidState("rollback failed for pattern %s", existing.pattern().patternUuid());
        }
        addPatternIndexes(owner, state, existing.pattern());
        return false;
    }

    @Override
    public boolean addCellPort(UUID machineId, int priority, int slotIndex, IPatternCellPort port) {
        var key = new CellKey(priority, machineId, slotIndex);
        var currentPriority = machinePriority.get(machineId);
        if (currentPriority != null && currentPriority != priority) {
            return false;
        }
        if (cellsByKey.containsKey(key)) {
            return false;
        }

        var patterns = List.copyOf(port.patterns());
        for (var pattern : patterns) {
            if (containsPatternUuid(pattern.patternUuid())) {
                return false;
            }
        }

        var state = new CellState(port, new HashSet<>());
        cellsByKey.put(key, state);
        orderedCells.add(key);
        machinePriority.putIfAbsent(machineId, priority);

        for (var pattern : patterns) {
            addPatternIndexes(key, state, pattern);
        }
        revision++;
        return true;
    }

    @Override
    public int removeCellPorts(UUID machineId) {
        var keys = orderedCells.stream().filter(key -> key.machineId().equals(machineId)).toList();
        if (keys.isEmpty()) {
            return 0;
        }

        for (var key : keys) {
            var state = requireCellState(key);
            for (var patternUuid : List.copyOf(state.patternUuidsInCell())) {
                var entry = byPatternUuid.get(patternUuid);
                if (entry == null || !entry.owner().equals(key)) {
                    throw invalidState("pattern %s missing or mismatched during cell removal", patternUuid);
                }
                removePatternIndexes(key, state, entry.pattern());
            }
            cellsByKey.remove(key);
            orderedCells.remove(key);
        }
        machinePriority.remove(machineId);
        revision++;
        return keys.size();
    }

    @Override
    public void clear() {
        if (cellsByKey.isEmpty() && orderedCells.isEmpty() && byPatternUuid.isEmpty() && byOutput.isEmpty() &&
            machinePriority.isEmpty()) {
            return;
        }
        cellsByKey.clear();
        orderedCells.clear();
        byPatternUuid.clear();
        byOutput.clear();
        machinePriority.clear();
        revision++;
    }

    private boolean addPatternToCell(CraftPattern pattern) {
        for (var key : orderedCells) {
            var state = requireCellState(key);
            if (!state.port().insert(pattern)) {
                continue;
            }
            addPatternIndexes(key, state, pattern);
            return true;
        }
        return false;
    }

    private void addPatternIndexes(CellKey owner, CellState state, CraftPattern pattern) {
        var existing = byPatternUuid.putIfAbsent(pattern.patternUuid(), new PatternEntry(pattern, owner));
        if (existing != null) {
            throw invalidState("duplicate pattern uuid %s in index", pattern.patternUuid());
        }
        if (!state.patternUuidsInCell().add(pattern.patternUuid())) {
            throw invalidState("duplicate pattern uuid %s in cell membership", pattern.patternUuid());
        }
        for (var output : pattern.outputs()) {
            var list = byOutput.computeIfAbsent(output.key(), $ -> new ArrayList<>());
            list.add(pattern);
            list.sort(DISPLAY_ORDER);
        }
    }

    private void removePatternIndexes(CellKey owner, CellState state, CraftPattern pattern) {
        var removed = byPatternUuid.remove(pattern.patternUuid());
        if (removed == null || !removed.owner().equals(owner)) {
            throw invalidState("pattern %s index owner mismatch during remove", pattern.patternUuid());
        }
        if (!state.patternUuidsInCell().remove(pattern.patternUuid())) {
            throw invalidState("pattern %s missing from cell membership during remove", pattern.patternUuid());
        }
        for (var output : pattern.outputs()) {
            var list = byOutput.get(output.key());
            if (list == null) {
                throw invalidState(
                    "output key %s missing while removing pattern %s",
                    output.key(),
                    pattern.patternUuid());
            }
            var removedPattern = list.removeIf(existing -> existing.patternUuid().equals(pattern.patternUuid()));
            if (!removedPattern) {
                throw invalidState("pattern %s missing from output key %s", pattern.patternUuid(), output.key());
            }
            if (list.isEmpty()) {
                byOutput.remove(output.key());
            }
        }
    }

    private CellState requireCellState(CellKey key) {
        var state = cellsByKey.get(key);
        if (state == null) {
            throw invalidState("cell %s missing from index", key);
        }
        return state;
    }

    private IllegalStateException invalidState(String template, Object... args) {
        var message = template.formatted(args);
        LOGGER.error("PatternRegistryCache invariant violation: {}", message);
        return new IllegalStateException(message);
    }

    private record CellKey(int priority, UUID machineId, int slotIndex) implements Comparable<CellKey> {
        @Override
        public int compareTo(CellKey other) {
            var priorityCmp = Integer.compare(other.priority, priority);
            if (priorityCmp != 0) {
                return priorityCmp;
            }
            var machineCmp = machineId.compareTo(other.machineId);
            if (machineCmp != 0) {
                return machineCmp;
            }
            return Integer.compare(slotIndex, other.slotIndex);
        }
    }

    private record CellState(IPatternCellPort port, Set<UUID> patternUuidsInCell) {}

    private record PatternEntry(CraftPattern pattern, CellKey owner) {}
}
