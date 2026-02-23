package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class NetworkPatternCell {
    private final UUID machineId;
    private final BlockPos subnet;
    private final int priority;
    private final int slotIndex;
    private final int bytesCapacity;
    private final List<CraftPattern> patterns = new ArrayList<>();

    public NetworkPatternCell(UUID machineId, BlockPos subnet, int priority, int slotIndex, int bytesCapacity) {
        this.machineId = machineId;
        this.subnet = subnet;
        this.priority = priority;
        this.slotIndex = slotIndex;
        this.bytesCapacity = bytesCapacity;
    }

    public UUID machineId() {
        return machineId;
    }

    public BlockPos subnet() {
        return subnet;
    }

    public int priority() {
        return priority;
    }

    public int slotIndex() {
        return slotIndex;
    }

    public List<CraftPattern> patterns() {
        return List.copyOf(patterns);
    }

    public boolean insert(CraftPattern pattern) {
        for (var existing : patterns) {
            if (existing.patternId().equals(pattern.patternId())) {
                return true;
            }
        }
        if ((patterns.size() + 1) * PatternCellStorage.BYTES_PER_PATTERN > bytesCapacity) {
            return false;
        }
        patterns.add(pattern);
        return true;
    }

    public boolean remove(String patternId) {
        return patterns.removeIf(pattern -> pattern.patternId().equals(patternId));
    }

    public static final Comparator<NetworkPatternCell> ORDER = Comparator
        .comparingInt(NetworkPatternCell::priority).reversed()
        .thenComparing(NetworkPatternCell::machineId)
        .thenComparingInt(NetworkPatternCell::slotIndex);
}
