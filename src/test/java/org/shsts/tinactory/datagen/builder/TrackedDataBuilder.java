package org.shsts.tinactory.datagen.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.context.TrackedContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class TrackedDataBuilder<V, U extends V, P, S extends TrackedDataBuilder<V, U, P, S>>
        extends DataBuilder<P, S> {
    protected final TrackedContext<V> ctx;
    protected final Supplier<U> object;
    protected final List<Runnable> callbacks = new ArrayList<>();

    public TrackedDataBuilder(DataGen dataGen, P parent, ResourceLocation loc,
                              TrackedContext<V> ctx, Supplier<U> object) {
        super(dataGen, parent, loc);
        this.ctx = ctx;
        this.object = object;
    }

    protected abstract void doRegister();

    @Override
    protected void register() {
        doRegister();
        for (var cb : callbacks) {
            cb.run();
        }
        callbacks.clear();
        ctx.process(object);
    }
}
