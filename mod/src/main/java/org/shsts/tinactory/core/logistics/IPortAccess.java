package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPortFilter;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPortAccess<T, P extends IPortFilter<T>> {
    boolean acceptInput(P port, T stack);

    boolean acceptOutput(P port);

    T insert(P port, T stack, boolean simulate);

    T extract(P port, T stack, boolean simulate);

    T extract(P port, int limit, boolean simulate);

    int getStorageAmount(P port, T stack);

    Collection<T> getAllStorages(P port);
}
