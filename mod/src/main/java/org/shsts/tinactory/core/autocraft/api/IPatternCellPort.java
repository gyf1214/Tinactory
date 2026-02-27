package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.logistics.IBytesProvider;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPatternCellPort extends IBytesProvider {
    List<CraftPattern> patterns();

    boolean insert(CraftPattern pattern);

    boolean remove(String patternId);
}
