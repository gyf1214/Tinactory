package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;

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

    default T stackOf(IStackKey key) {
        return stackOf(key, 1);
    }

    IRenderDescriptor display(T stack);

    default IRenderDescriptor display(IStackKey key) {
        return display(stackOf(key));
    }

    Component name(T stack);

    default Component name(IStackKey key) {
        return name(stackOf(key));
    }

    Optional<List<Component>> tooltip(T stack);

    default Optional<List<Component>> tooltip(IStackKey key) {
        return tooltip(stackOf(key));
    }
}
