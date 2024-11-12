package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ILogisticsContentWrapper {
    int getCount();

    boolean isEmpty();

    void grow(int amount);

    void shrink(int amount);

    ILogisticsContentWrapper extractFrom(IPort port, boolean simulate);

    ILogisticsContentWrapper insertInto(IPort port, boolean simulate);

    ILogisticsTypeWrapper getType();

    default PortType getPortType() {
        return getType().getPortType();
    }

    ILogisticsContentWrapper copyWithAmount(int amount);

    static boolean canStack(ILogisticsContentWrapper me, ILogisticsContentWrapper other) {
        return me.getType().equals(other.getType());
    }
}
