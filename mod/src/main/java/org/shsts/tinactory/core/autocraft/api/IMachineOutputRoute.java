package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineOutputRoute {
    CraftKey key();

    long pull(long amount, boolean simulate);
}
