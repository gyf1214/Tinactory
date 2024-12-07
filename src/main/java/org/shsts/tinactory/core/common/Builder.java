package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinycorelib.api.core.IBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class Builder<U, P, S extends IBuilder<U, P, S>> implements IBuilder<U, P, S> {
    protected final P parent;
    protected final List<Consumer<U>> onCreateObject = new ArrayList<>();
    protected final List<Runnable> onBuild = new ArrayList<>();

    protected Builder(P parent) {
        this.parent = parent;
    }

    protected abstract U createObject();

    @Override
    public U buildObject() {
        var object = createObject();
        for (var cb : onCreateObject) {
            cb.accept(object);
        }
        onCreateObject.clear();
        return object;
    }

    @Override
    public P build() {
        for (var cb : onBuild) {
            cb.run();
        }
        onBuild.clear();
        return parent;
    }

    @Override
    public S onCreateObject(Consumer<U> cons) {
        onCreateObject.add(cons);
        return self();
    }

    @Override
    public S onBuild(Runnable cb) {
        onBuild.add(cb);
        return self();
    }
}
