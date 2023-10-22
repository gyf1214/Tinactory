package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.ISelf;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Builder<U, P, S extends Builder<U, P, S>> implements ISelf<S> {
    public final String id;
    public final ResourceLocation loc;

    protected final Registrate registrate;
    protected final P parent;
    protected final List<Consumer<U>> onCreateObject = new ArrayList<>();

    public Builder(Registrate registrate, P parent, String id) {
        this.registrate = registrate;
        this.parent = parent;
        this.id = id;
        this.loc = new ResourceLocation(registrate.modid, id);
    }

    public Builder(Registrate registrate, P parent, ResourceLocation loc) {
        this.registrate = registrate;
        this.parent = parent;
        this.id = loc.getPath();
        this.loc = loc;
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
        this.buildObject();
        return this.parent;
    }

    public S transform(Transformer<S> trans) {
        return trans.apply(self());
    }
}
