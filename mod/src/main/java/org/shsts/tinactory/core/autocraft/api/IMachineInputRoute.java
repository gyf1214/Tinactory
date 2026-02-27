package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineInputRoute {
    CraftKey key();

    long push(long amount, boolean simulate);
}
