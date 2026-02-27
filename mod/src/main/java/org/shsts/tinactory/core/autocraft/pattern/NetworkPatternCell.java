package org.shsts.tinactory.core.autocraft.pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;

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
    private final IPatternCellPort patternPort;

    public NetworkPatternCell(
        UUID machineId,
        BlockPos subnet,
        int priority,
        int slotIndex,
        IPatternCellPort patternPort) {
        this.machineId = machineId;
        this.subnet = subnet;
        this.priority = priority;
        this.slotIndex = slotIndex;
        this.patternPort = patternPort;
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
        return patternPort.patterns();
    }

    public boolean insert(CraftPattern pattern) {
        return patternPort.insert(pattern);
    }

    public boolean remove(String patternId) {
        return patternPort.remove(patternId);
    }

    public static final Comparator<NetworkPatternCell> ORDER = Comparator
        .comparingInt(NetworkPatternCell::priority).reversed()
        .thenComparing(NetworkPatternCell::machineId)
        .thenComparingInt(NetworkPatternCell::slotIndex);
}
