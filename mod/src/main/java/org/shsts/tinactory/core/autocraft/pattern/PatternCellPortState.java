package org.shsts.tinactory.core.autocraft.pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternCellPortState implements IPatternCellPort {
    public static final String PATTERNS_KEY = "patterns";
    public static final int BYTES_PER_PATTERN = 256;

    private final int bytesLimit;
    private final PatternNbtCodec codec = new PatternNbtCodec(new MachineConstraintRegistry());
    private final Map<String, CraftPattern> patterns = new HashMap<>();

    public PatternCellPortState(int bytesLimit) {
        this.bytesLimit = bytesLimit;
    }

    @Override
    public int bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public int bytesUsed() {
        return patterns.size() * BYTES_PER_PATTERN;
    }

    @Override
    public List<CraftPattern> patterns() {
        return patterns.values().stream().toList();
    }

    @Override
    public boolean insert(CraftPattern pattern) {
        if (patterns.containsKey(pattern.patternId())) {
            return true;
        }
        if ((patterns.size() + 1) * BYTES_PER_PATTERN > bytesLimit) {
            return false;
        }
        patterns.put(pattern.patternId(), pattern);
        return true;
    }

    @Override
    public boolean remove(String patternId) {
        return patterns.remove(patternId) != null;
    }

    public CompoundTag serialize() {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (var pattern : patterns.values()) {
            list.add(codec.encodePattern(pattern));
        }
        tag.put(PATTERNS_KEY, list);
        return tag;
    }

    public void deserialize(CompoundTag tag) {
        patterns.clear();
        var list = tag.getList(PATTERNS_KEY, Tag.TAG_COMPOUND);
        for (var tag1 : list) {
            var pattern = codec.decodePattern((CompoundTag) tag1);
            patterns.put(pattern.patternId(), pattern);
        }
    }
}
