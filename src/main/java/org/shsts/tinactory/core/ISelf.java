package org.shsts.tinactory.core;

public interface ISelf<S extends ISelf<S>> {
    @SuppressWarnings("unchecked")
    default S self() {
        return (S) this;
    }
}
