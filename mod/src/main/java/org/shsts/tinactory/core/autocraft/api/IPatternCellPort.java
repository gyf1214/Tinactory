package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.logistics.IBytesProvider;

import java.util.Collection;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPatternCellPort extends IBytesProvider {
    Collection<CraftPattern> patterns();

    boolean insert(CraftPattern pattern);

    boolean remove(UUID patternUuid);
}
