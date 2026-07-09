package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternCellData;

import java.util.Collection;
import java.util.UUID;

import static org.shsts.tinactory.AllDataComponents.ME_PATTERN_CELL_CONTENT;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class PatternCellPort implements IPatternCellPort {
    private final ItemStack stack;
    private final long bytesLimit;
    private final long bytesPerPattern;

    public PatternCellPort(ItemStack stack, long bytesLimit, long bytesPerPattern) {
        this.stack = stack;
        this.bytesLimit = bytesLimit;
        this.bytesPerPattern = bytesPerPattern;
    }

    @Override
    public long bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public long bytesUsed() {
        return data().patternCount() * bytesPerPattern;
    }

    @Override
    public Collection<CraftPattern> patterns() {
        return data().patterns();
    }

    @Override
    public boolean insert(CraftPattern pattern) {
        var data = data();
        if (data.patternsById().containsKey(pattern.patternUuid())) {
            return true;
        }
        if (bytesPerPattern > 0L && (long) data.patternCount() + 1L > bytesLimit / bytesPerPattern) {
            return false;
        }
        write(data.withPattern(pattern));
        return true;
    }

    @Override
    public boolean remove(UUID patternUuid) {
        var data = data();
        var next = data.withoutPattern(patternUuid);
        if (next == data) {
            return false;
        }
        write(next);
        return true;
    }

    private PatternCellData data() {
        return stack.getOrDefault(ME_PATTERN_CELL_CONTENT.get(), PatternCellData.EMPTY);
    }

    private void write(PatternCellData data) {
        if (data.patternCount() == 0) {
            stack.remove(ME_PATTERN_CELL_CONTENT.get());
        } else {
            stack.set(ME_PATTERN_CELL_CONTENT.get(), data);
        }
    }

}
