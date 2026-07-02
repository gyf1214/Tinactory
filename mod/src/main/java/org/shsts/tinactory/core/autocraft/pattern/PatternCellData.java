package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class PatternCellData {
    public static final PatternCellData EMPTY = new PatternCellData(Map.of());

    private final Map<UUID, CraftPattern> patterns;
    private final int patternCount;

    private PatternCellData(Map<UUID, CraftPattern> patterns) {
        this.patterns = Collections.unmodifiableMap(new LinkedHashMap<>(patterns));
        patternCount = this.patterns.size();
    }

    public static PatternCellData of(Map<UUID, CraftPattern> patterns) {
        var copied = new LinkedHashMap<UUID, CraftPattern>();
        for (var entry : patterns.entrySet()) {
            var pattern = entry.getValue();
            copied.put(pattern.patternUuid(), pattern);
        }
        return copied.isEmpty() ? EMPTY : new PatternCellData(copied);
    }

    public Map<UUID, CraftPattern> patternsById() {
        return patterns;
    }

    public Collection<CraftPattern> patterns() {
        return patterns.values();
    }

    public int patternCount() {
        return patternCount;
    }

    public PatternCellData withPattern(CraftPattern pattern) {
        if (patterns.containsKey(pattern.patternUuid())) {
            return this;
        }
        var next = new LinkedHashMap<>(patterns);
        next.put(pattern.patternUuid(), pattern);
        return new PatternCellData(next);
    }

    public PatternCellData withoutPattern(UUID patternUuid) {
        if (!patterns.containsKey(patternUuid)) {
            return this;
        }
        var next = new LinkedHashMap<>(patterns);
        next.remove(patternUuid);
        return next.isEmpty() ? EMPTY : new PatternCellData(next);
    }

    public static Codec<PatternCellData> codec(Codec<CraftPattern> patternCodec) {
        return patternCodec.listOf().xmap(PatternCellData::fromPatterns, PatternCellData::toPatterns);
    }

    private static PatternCellData fromPatterns(List<CraftPattern> patterns) {
        var map = new LinkedHashMap<UUID, CraftPattern>();
        for (var pattern : patterns) {
            map.put(pattern.patternUuid(), pattern);
        }
        return of(map);
    }

    private static List<CraftPattern> toPatterns(PatternCellData data) {
        return new ArrayList<>(data.patterns.values());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof PatternCellData data && patterns.equals(data.patterns));
    }

    @Override
    public int hashCode() {
        return Objects.hash(patterns);
    }
}
