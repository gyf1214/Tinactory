package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortType;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ILogisticsTypeWrapper {
    PortType getPortType();
}
