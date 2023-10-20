package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.network.Scheduling;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SchedulingBuilder<P> extends RegistryEntryBuilder<Scheduling, Scheduling, P, SchedulingBuilder<P>> {
    private final List<Supplier<Supplier<Scheduling>>> befores = new ArrayList<>();
    private final List<Supplier<Supplier<Scheduling>>> afters = new ArrayList<>();

    public SchedulingBuilder(Registrate registrate, RegistryEntryHandler<Scheduling> handler, String id, P parent) {
        super(registrate, handler, id, parent);
    }

    @SafeVarargs
    public final SchedulingBuilder<P> before(Supplier<Supplier<Scheduling>>... befores) {
        this.befores.addAll(List.of(befores));
        return self();
    }

    @SafeVarargs
    public final SchedulingBuilder<P> after(Supplier<Supplier<Scheduling>>... afters) {
        this.afters.addAll(List.of(afters));
        return self();
    }

    @Override
    public Scheduling createObject() {
        return new Scheduling(this.befores.stream().map(Supplier::get).toList(),
                this.afters.stream().map(Supplier::get).toList());
    }
}
