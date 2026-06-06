package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternCellPortState implements IPatternCellPort {
    public static final String PATTERNS_KEY = "patterns";

    private final long bytesPerPattern;
    private final long bytesLimit;
    private final PatternNbtCodec codec;
    private final Map<UUID, CraftPattern> patterns = new HashMap<>();

    public PatternCellPortState(
        long bytesPerPattern,
        long bytesLimit,
        Codec<IMachineConstraint> constraintCodec,
        Codec<IStackKey> keyCodec) {
        this(bytesPerPattern, bytesLimit, new PatternNbtCodec(constraintCodec, keyCodec));
    }

    public PatternCellPortState(long bytesPerPattern, long bytesLimit, PatternNbtCodec codec) {
        this.bytesPerPattern = bytesPerPattern;
        this.bytesLimit = bytesLimit;
        this.codec = codec;
    }

    @Override
    public long bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public long bytesUsed() {
        return saturatedMultiply(patterns.size(), bytesPerPattern);
    }

    @Override
    public List<CraftPattern> patterns() {
        return patterns.values().stream().toList();
    }

    @Override
    public boolean insert(CraftPattern pattern) {
        if (patterns.containsKey(pattern.patternUuid())) {
            return true;
        }
        if (bytesPerPattern > 0L && (long) patterns.size() + 1L > bytesLimit / bytesPerPattern) {
            return false;
        }
        patterns.put(pattern.patternUuid(), pattern);
        return true;
    }

    @Override
    public boolean remove(UUID patternUuid) {
        return patterns.remove(patternUuid) != null;
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
            patterns.put(pattern.patternUuid(), pattern);
        }
    }

    private static long saturatedMultiply(long left, long right) {
        if (left == 0L || right == 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }
}
