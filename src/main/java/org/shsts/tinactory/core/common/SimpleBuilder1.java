package org.shsts.tinactory.core.common;

public abstract class SimpleBuilder1<U, P, S extends SimpleBuilder1<U, P, S>>
    extends XBuilderBase<U, P, S> {
    protected SimpleBuilder1(P parent) {
        super(parent);
        onBuild.add(this::buildObject);
    }
}
