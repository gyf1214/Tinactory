package org.shsts.tinactory.core;

@FunctionalInterface
public interface Transformer<T> {
    T apply(T t);

    default Transformer<T> chain(Transformer<T> other) {
        return $ -> other.apply(this.apply($));
    }
}
