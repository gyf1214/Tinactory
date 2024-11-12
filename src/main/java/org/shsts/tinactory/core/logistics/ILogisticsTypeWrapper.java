package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ILogisticsTypeWrapper {
    PortType getPortType();
}
