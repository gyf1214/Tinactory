package org.shsts.tinactory.core.common;

public abstract class SimpleBuilder<U, P, S extends SimpleBuilder<U, P, S>>
    extends XBuilderBase<U, P, S> {
    protected SimpleBuilder(P parent) {
        super(parent);
        onBuild.add(this::buildObject);
    }
}
