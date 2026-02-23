package org.shsts.tinactory.core.autocraft.integration;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_LIST;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternCellStorage {
    public static final String PATTERNS_KEY = "patterns";
    public static final int BYTES_PER_PATTERN = 256;
    private static final Logger LOGGER = LogUtils.getLogger();

    private PatternCellStorage() {}

    public static List<CraftPattern> listPatterns(CompoundTag tag, PatternNbtCodec codec) {
        if (!tag.contains(PATTERNS_KEY, TAG_LIST)) {
            return List.of();
        }
        var raw = tag.getList(PATTERNS_KEY, TAG_COMPOUND);
        var out = new ArrayList<CraftPattern>(raw.size());
        for (var i = 0; i < raw.size(); i++) {
            try {
                out.add(codec.decodePattern(raw.getCompound(i)));
            } catch (RuntimeException ex) {
                LOGGER.warn("skip invalid autocraft pattern entry at {}", i, ex);
            }
        }
        return out;
    }

    public static int bytesUsed(CompoundTag tag, PatternNbtCodec codec) {
        return listPatterns(tag, codec).size() * BYTES_PER_PATTERN;
    }

    public static boolean insertPattern(CompoundTag tag, int bytesLimit, CraftPattern pattern, PatternNbtCodec codec) {
        var patterns = listPatterns(tag, codec);
        for (var existing : patterns) {
            if (existing.patternId().equals(pattern.patternId())) {
                return true;
            }
        }
        if ((patterns.size() + 1) * BYTES_PER_PATTERN > bytesLimit) {
            return false;
        }
        var list = tag.getList(PATTERNS_KEY, TAG_COMPOUND);
        list.add(codec.encodePattern(pattern));
        tag.put(PATTERNS_KEY, list);
        return true;
    }

    public static void clear(CompoundTag tag) {
        tag.remove(PATTERNS_KEY);
    }
}
