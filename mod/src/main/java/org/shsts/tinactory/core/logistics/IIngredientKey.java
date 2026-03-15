package org.shsts.tinactory.core.logistics;

import org.shsts.tinactory.api.logistics.PortType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IIngredientKey extends Comparable<IIngredientKey> {
    default PortType type() {
        return PortType.NONE;
    }

    @Override
    default int compareTo(IIngredientKey other) {
        throw new UnsupportedOperationException("compareTo is not implemented");
    }
}
