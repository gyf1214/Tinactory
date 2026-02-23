package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternCellPortState implements IPatternCellPort {
    public static final int BYTES_PER_PATTERN = PatternCellStorage.BYTES_PER_PATTERN;

    private final int bytesLimit;
    private final PatternNbtCodec codec = new PatternNbtCodec(new MachineConstraintRegistry());
    private CompoundTag tag = new CompoundTag();

    public PatternCellPortState(int bytesLimit) {
        this.bytesLimit = bytesLimit;
    }

    @Override
    public int bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public int bytesUsed() {
        return PatternCellStorage.bytesUsed(tag, codec);
    }

    @Override
    public List<CraftPattern> patterns() {
        return PatternCellStorage.listPatterns(tag, codec);
    }

    @Override
    public boolean insert(CraftPattern pattern) {
        return PatternCellStorage.insertPattern(tag, bytesLimit, pattern, codec);
    }

    @Override
    public boolean remove(String patternId) {
        var old = PatternCellStorage.listPatterns(tag, codec);
        if (old.stream().noneMatch($ -> $.patternId().equals(patternId))) {
            return false;
        }
        PatternCellStorage.clear(tag);
        for (var pattern : old) {
            if (!pattern.patternId().equals(patternId)) {
                PatternCellStorage.insertPattern(tag, bytesLimit, pattern, codec);
            }
        }
        return true;
    }

    public CompoundTag serialize() {
        return tag.copy();
    }

    public void deserialize(CompoundTag tag) {
        this.tag = tag.copy();
    }
}
