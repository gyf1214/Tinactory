package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.logistics.IStackKey;
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

    private final Map<CellKey, CellState> cellsByKey = new HashMap<>();
    private final NavigableSet<CellKey> orderedCells = new TreeSet<>();
    private final Map<String, PatternEntry> byPatternId = new HashMap<>();
    private final Map<IStackKey, List<CraftPattern>> byOutput = new HashMap<>();
    private final Map<UUID, Integer> machinePriority = new HashMap<>();

    @Override
    public List<CraftPattern> findPatternsProducing(IStackKey key) {
        return List.copyOf(byOutput.getOrDefault(key, List.of()));
    }

    @Override
    public List<IStackKey> listRequestables() {
        return byOutput.keySet().stream().sorted().toList();
    }

    @Override
    public boolean containsPatternId(String patternId) {
        return byPatternId.containsKey(patternId);
    }

    @Override
    public boolean addPattern(CraftPattern pattern) {
        if (containsPatternId(pattern.patternId())) {
            return false;
        }
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

    @Override
    public boolean removePattern(String patternId) {
        var entry = byPatternId.get(patternId);
        if (entry == null) {
            return false;
        }
        var state = requireCellState(entry.owner());
        if (!state.port().remove(patternId)) {
            throw invalidState("pattern %s exists in indexes but owner cell rejected remove", patternId);
        }
        removePatternIndexes(entry.owner(), state, entry.pattern());
        return true;
    }

    @Override
    public boolean updatePattern(CraftPattern pattern) {
        var existing = byPatternId.get(pattern.patternId());
        if (existing == null) {
            return addPattern(pattern);
        }

        var owner = existing.owner();
        var state = requireCellState(owner);
        if (!state.port().remove(existing.pattern().patternId())) {
            throw invalidState(
                "pattern %s exists in indexes but owner cell rejected remove",
                existing.pattern().patternId());
        }
        removePatternIndexes(owner, state, existing.pattern());

        if (addPattern(pattern)) {
            return true;
        }

        if (!state.port().insert(existing.pattern())) {
            throw invalidState("rollback failed for pattern %s", existing.pattern().patternId());
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
            if (containsPatternId(pattern.patternId())) {
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
            for (var patternId : List.copyOf(state.patternIdsInCell())) {
                var entry = byPatternId.get(patternId);
                if (entry == null || !entry.owner().equals(key)) {
                    throw invalidState("pattern %s missing or mismatched during cell removal", patternId);
                }
                removePatternIndexes(key, state, entry.pattern());
            }
            cellsByKey.remove(key);
            orderedCells.remove(key);
        }
        machinePriority.remove(machineId);
        return keys.size();
    }

    @Override
    public void clear() {
        cellsByKey.clear();
        orderedCells.clear();
        byPatternId.clear();
        byOutput.clear();
        machinePriority.clear();
    }

    private void addPatternIndexes(CellKey owner, CellState state, CraftPattern pattern) {
        var existing = byPatternId.putIfAbsent(pattern.patternId(), new PatternEntry(pattern, owner));
        if (existing != null) {
            throw invalidState("duplicate pattern id %s in index", pattern.patternId());
        }
        if (!state.patternIdsInCell().add(pattern.patternId())) {
            throw invalidState("duplicate pattern id %s in cell membership", pattern.patternId());
        }
        for (var output : pattern.outputs()) {
            var list = byOutput.computeIfAbsent(output.key(), $ -> new ArrayList<>());
            list.add(pattern);
            list.sort(Comparator.comparing(CraftPattern::patternId));
        }
    }

    private void removePatternIndexes(CellKey owner, CellState state, CraftPattern pattern) {
        var removed = byPatternId.remove(pattern.patternId());
        if (removed == null || !removed.owner().equals(owner)) {
            throw invalidState("pattern %s index owner mismatch during remove", pattern.patternId());
        }
        if (!state.patternIdsInCell().remove(pattern.patternId())) {
            throw invalidState("pattern %s missing from cell membership during remove", pattern.patternId());
        }
        for (var output : pattern.outputs()) {
            var list = byOutput.get(output.key());
            if (list == null) {
                throw invalidState(
                    "output key %s missing while removing pattern %s",
                    output.key(),
                    pattern.patternId());
            }
            var removedPattern = list.removeIf(existing -> existing.patternId().equals(pattern.patternId()));
            if (!removedPattern) {
                throw invalidState("pattern %s missing from output key %s", pattern.patternId(), output.key());
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

    private record CellState(IPatternCellPort port, Set<String> patternIdsInCell) {}

    private record PatternEntry(CraftPattern pattern, CellKey owner) {}
}
