package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPatternRepository {
    List<CraftPattern> findPatternsProducing(CraftKey key);
}
