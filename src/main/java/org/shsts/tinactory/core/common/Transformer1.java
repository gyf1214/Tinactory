package org.shsts.tinactory.core.common;

@FunctionalInterface
public interface Transformer1<T> {
    T apply(T t);

    default Transformer1<T> chain(Transformer1<T> other) {
        return $ -> other.apply(apply($));
    }

    @SuppressWarnings("unchecked")
    default <U> Transformer1<U> cast() {
        return (Transformer1<U>) this;
    }
}
