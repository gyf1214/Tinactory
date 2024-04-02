package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BuilderBase<U, P, S extends BuilderBase<U, P, S>> implements ISelf<S> {
    protected final P parent;
    protected final List<Consumer<U>> onCreateObject = new ArrayList<>();
    protected final List<Consumer<S>> onBuild = new ArrayList<>();

    protected BuilderBase(P parent) {
        this.parent = parent;
    }

    public abstract U createObject();

    public U buildObject() {
        var object = this.createObject();
        for (var cb : this.onCreateObject) {
            cb.accept(object);
        }
        this.onCreateObject.clear();
        return object;
    }

    public P build() {
        for (var cb : this.onBuild) {
            cb.accept(self());
        }
        this.onBuild.clear();
        return this.parent;
    }

    public S transform(Transformer<S> trans) {
        return trans.apply(self());
    }
}
