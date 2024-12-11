package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class XBuilderBase<U, P, S extends XBuilderBase<U, P, S>> implements ISelf<S> {
    protected final P parent;
    protected final List<Consumer<U>> onCreateObject = new ArrayList<>();
    protected final List<Runnable> onBuild = new ArrayList<>();

    protected XBuilderBase(P parent) {
        this.parent = parent;
    }

    protected abstract U createObject();

    public U buildObject() {
        var object = createObject();
        for (var cb : onCreateObject) {
            cb.accept(object);
        }
        onCreateObject.clear();
        return object;
    }

    public P build() {
        for (var cb : onBuild) {
            cb.run();
        }
        onBuild.clear();
        return parent;
    }

    public S transform(Transformer1<S> trans) {
        return trans.apply(self());
    }

    public S onCreateObject(Consumer<U> cons) {
        onCreateObject.add(cons);
        return self();
    }
}
