package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentBuilder<U, T, P> extends SimpleBuilder<Map<Voltage, IEntry<U>>,
    P, ComponentBuilder<U, T, P>> {
    protected record Pair<T>(Voltage voltage, T parameter) {}

    protected final List<Pair<T>> voltages = new ArrayList<>();
    private final BiFunction<Voltage, T, IEntry<U>> factory;

    protected ComponentBuilder(P parent, BiFunction<Voltage, T, IEntry<U>> factory) {
        super(parent);
        this.factory = factory;
    }

    public ComponentBuilder<U, T, P> voltage(Voltage v, T p) {
        voltages.add(new Pair<>(v, p));
        return this;
    }

    @Override
    protected Map<Voltage, IEntry<U>> createObject() {
        return voltages.stream().collect(Collectors.toMap(Pair::voltage,
            $ -> factory.apply($.voltage, $.parameter)));
    }

    public static class Simple<U, P> extends ComponentBuilder<U, Unit, P> {
        private Simple(P parent, Function<Voltage, IEntry<U>> factory) {
            super(parent, (v, $) -> factory.apply(v));
        }

        public Simple<U, P> voltage(Voltage v) {
            voltages.add(new Pair<>(v, Unit.INSTANCE));
            return this;
        }

        public Simple<U, P> voltages(Voltage from, Voltage to) {
            Voltage.between(from, to).forEach(this::voltage);
            return this;
        }
    }

    public static <U, T, P> ComponentBuilder<U, T, P> builder(
        P parent, BiFunction<Voltage, T, IEntry<U>> factory) {
        return new ComponentBuilder<>(parent, factory);
    }

    public static <U, T> ComponentBuilder<U, T, ?> builder(
        BiFunction<Voltage, T, IEntry<U>> factory) {
        return builder(Unit.INSTANCE, factory);
    }

    public static <U, P> Simple<U, P> simple(P parent,
        Function<Voltage, IEntry<U>> factory) {
        return new Simple<>(parent, factory);
    }

    public static <U> Simple<U, ?> simple(Function<Voltage, IEntry<U>> factory) {
        return simple(Unit.INSTANCE, factory);
    }
}
