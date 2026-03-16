package org.shsts.tinactory.integration.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.logistics.IIngredientKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsPatternRepository implements IPatternRepository {
    private final Map<IIngredientKey, List<CraftPattern>> byOutput = new HashMap<>();

    public LogisticsPatternRepository(List<CraftPattern> patterns) {
        for (var pattern : patterns) {
            for (var output : pattern.outputs()) {
                byOutput.computeIfAbsent(output.key(), $ -> new ArrayList<>()).add(pattern);
            }
        }
        for (var entry : byOutput.values()) {
            entry.sort(Comparator.comparing(CraftPattern::patternId));
        }
    }

    @Override
    public List<CraftPattern> findPatternsProducing(IIngredientKey key) {
        return byOutput.getOrDefault(key, List.of());
    }

    @Override
    public List<IIngredientKey> listRequestables() {
        return byOutput.keySet().stream().sorted().toList();
    }

    @Override
    public boolean containsPatternId(String patternId) {
        return byOutput.values().stream()
            .flatMap(List::stream)
            .anyMatch(pattern -> pattern.patternId().equals(patternId));
    }

    @Override
    public boolean addPattern(CraftPattern pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removePattern(String patternId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updatePattern(CraftPattern pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addCellPort(UUID machineId, int priority, int slotIndex, IPatternCellPort port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int removeCellPorts(UUID machineId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {}
}
