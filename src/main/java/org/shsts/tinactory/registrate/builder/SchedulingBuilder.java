package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.network.Scheduling;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SchedulingBuilder<P> extends RegistryEntryBuilder<IScheduling, IScheduling, P, SchedulingBuilder<P>> {
    private final List<Supplier<Supplier<IScheduling>>> befores = new ArrayList<>();
    private final List<Supplier<Supplier<IScheduling>>> afters = new ArrayList<>();

    public SchedulingBuilder(Registrate registrate, RegistryEntryHandler<IScheduling> handler, String id, P parent) {
        super(registrate, handler, id, parent);
    }

    @SafeVarargs
    public final SchedulingBuilder<P> before(Supplier<Supplier<IScheduling>>... befores) {
        this.befores.addAll(List.of(befores));
        return self();
    }

    @SafeVarargs
    public final SchedulingBuilder<P> after(Supplier<Supplier<IScheduling>>... afters) {
        this.afters.addAll(List.of(afters));
        return self();
    }

    @Override
    public IScheduling createObject() {
        return new Scheduling(this.befores.stream().map(Supplier::get).toList(),
                this.afters.stream().map(Supplier::get).toList());
    }
}
