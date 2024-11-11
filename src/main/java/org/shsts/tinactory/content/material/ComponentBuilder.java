package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentBuilder<U, T, P> extends SimpleBuilder<Map<Voltage, RegistryEntry<U>>,
        P, ComponentBuilder<U, T, P>> {
    protected record Pair<T>(Voltage voltage, T parameter) {}

    protected final List<Pair<T>> voltages = new ArrayList<>();
    private final BiFunction<Voltage, T, RegistryEntry<U>> factory;

    protected ComponentBuilder(P parent, BiFunction<Voltage, T, RegistryEntry<U>> factory) {
        super(parent);
        this.factory = factory;
    }

    public ComponentBuilder<U, T, P> voltage(Voltage v, T p) {
        voltages.add(new Pair<>(v, p));
        return this;
    }


    @Override
    protected Map<Voltage, RegistryEntry<U>> createObject() {
        return voltages.stream().collect(Collectors.toMap(Pair::voltage,
                $ -> factory.apply($.voltage, $.parameter)));
    }

    public static class DummyBuilder<U, P> extends ComponentBuilder<U, Unit, P> {
        public DummyBuilder(P parent, Function<Voltage, RegistryEntry<U>> factory) {
            super(parent, (v, $) -> factory.apply(v));
        }

        public DummyBuilder<U, P> voltage(Voltage v) {
            voltages.add(new Pair<>(v, Unit.INSTANCE));
            return this;
        }

        public DummyBuilder<U, P> voltages(Voltage from, Voltage to) {
            Voltage.between(from, to).forEach(this::voltage);
            return this;
        }
    }

    public static <U, T> ComponentBuilder<U, T, ?>
    builder(BiFunction<Voltage, T, RegistryEntry<U>> factory) {
        return new ComponentBuilder<>(Unit.INSTANCE, factory);
    }

    public static <U> DummyBuilder<U, ?>
    simple(Function<Voltage, RegistryEntry<U>> factory) {
        return new DummyBuilder<>(Unit.INSTANCE, factory);
    }
}
