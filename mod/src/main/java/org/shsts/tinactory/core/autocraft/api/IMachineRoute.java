package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineRoute {
    IStackKey key();

    PortDirection direction();

    long transfer(long amount, boolean simulate);
}
