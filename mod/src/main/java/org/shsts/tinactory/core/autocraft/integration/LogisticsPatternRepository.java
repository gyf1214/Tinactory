package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsPatternRepository implements IPatternRepository {
    private final Map<CraftKey, List<CraftPattern>> byOutput = new HashMap<>();

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
    public List<CraftPattern> findPatternsProducing(CraftKey key) {
        return byOutput.getOrDefault(key, List.of());
    }
}
