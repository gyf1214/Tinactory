package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
        assert this.value != null;
        return this.value;
    }

    public static <T1> ValueHolder<T1> create() {
        return new ValueHolder<>();
    }
}
