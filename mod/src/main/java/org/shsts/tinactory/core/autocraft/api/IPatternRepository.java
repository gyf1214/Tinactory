package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.logistics.IStackKey;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPatternRepository {
    List<CraftPattern> findPatternsProducing(IStackKey key);

    List<IStackKey> listRequestables();

    boolean containsPatternId(String patternId);

    boolean addPattern(CraftPattern pattern);

    boolean removePattern(String patternId);

    boolean updatePattern(CraftPattern pattern);

    boolean addCellPort(UUID machineId, int priority, int slotIndex, IPatternCellPort port);

    int removeCellPorts(UUID machineId);

    void clear();
}
