package org.shsts.tinactory.core.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.network.Scheduling;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.shsts.tinactory.registrate.AllRegistries.SCHEDULINGS;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SchedulingBuilder<P> extends Builder<IScheduling, P, SchedulingBuilder<P>> {
    private final IRegistrate registrate;
    private final String id;
    private final List<Supplier<Supplier<IScheduling>>> befores = new ArrayList<>();
    private final List<Supplier<Supplier<IScheduling>>> afters = new ArrayList<>();

    public SchedulingBuilder(IRegistrate registrate, P parent, String id) {
        super(parent);
        this.registrate = registrate;
        this.id = id;

        onBuild(this::register);
    }

    public final SchedulingBuilder<P> before(Supplier<Supplier<IScheduling>> before) {
        this.befores.add(before);
        return self();
    }

    public final SchedulingBuilder<P> after(Supplier<Supplier<IScheduling>> after) {
        this.afters.add(after);
        return self();
    }

    @Override
    protected IScheduling createObject() {
        return new Scheduling(befores.stream().map(Supplier::get).toList(),
            afters.stream().map(Supplier::get).toList());
    }

    public IEntry<IScheduling> register() {
        return registrate.registryEntry(SCHEDULINGS.getHandler(), id, this::buildObject);
    }
}
