package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IStackAdapter<T> {
    T empty();

    boolean isEmpty(T stack);

    T copy(T stack);

    int amount(T stack);

    T withAmount(T stack, int amount);

    boolean canStack(T left, T right);

    IIngredientKey keyOf(T stack);
}
