package org.shsts.tinactory.core.logistics;

import org.shsts.tinactory.api.logistics.PortType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IStackKey extends Comparable<IStackKey> {
    PortType type();
}
