package org.shsts.tinactory.core.common;

public abstract class SimpleBuilder<U, P, S extends SimpleBuilder<U, P, S>>
    extends BuilderBase<U, P, S> {
    protected SimpleBuilder(P parent) {
        super(parent);
        onBuild.add(this::buildObject);
    }
}
