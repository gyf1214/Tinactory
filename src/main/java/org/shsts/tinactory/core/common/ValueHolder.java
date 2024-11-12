package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ValueHolder<T> implements Supplier<T> {
    @Nullable
    private T value = null;

    private ValueHolder() {}

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        assert value != null;
        return value;
    }

    public static <T1> ValueHolder<T1> create() {
        return new ValueHolder<>();
    }
}
