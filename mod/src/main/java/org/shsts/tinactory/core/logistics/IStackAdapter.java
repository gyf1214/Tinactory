package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IStackAdapter<T> {
    T empty();

    boolean isEmpty(T stack);

    T copy(T stack);

    int amount(T stack);

    T withAmount(T stack, int amount);

    boolean canStack(T left, T right);

    IStackKey keyOf(T stack);

    T stackOf(IStackKey key, long amount);

    IRenderDescriptor display(T stack);

    Optional<List<Component>> tooltip(T stack);
}
