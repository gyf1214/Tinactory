package org.shsts.tinactory.core.common;

public interface ISelf<S extends ISelf<S>> {
    @SuppressWarnings("unchecked")
    default S self() {
        return (S) this;
    }
}
