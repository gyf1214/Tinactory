package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class GeneralUtil {
    public static <S, T> Function<S, Optional<T>> optionalCastor(Class<T> clazz) {
        return sth -> Optional.ofNullable(nullCast(sth, clazz));
    }

    public static <S, T> @Nullable T nullCast(@Nullable S sth, Class<T> clazz) {
        return clazz.isInstance(sth) ? clazz.cast(sth) : null;
    }
}
