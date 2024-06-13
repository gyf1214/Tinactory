package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet<U extends MachineBlock<?>> {
    public final Map<Voltage, Layout> layoutSet;
    public final Set<Voltage> voltages;
    protected final Map<Voltage, RegistryEntry<U>> machines;
    @Nullable
    protected final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive;

    public MachineSet(Set<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                      Map<Voltage, RegistryEntry<U>> machines,
                      @Nullable RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive) {
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.primitive = primitive;
        this.voltages = voltages;
    }

    public RegistryEntry<? extends Block> entry(Voltage voltage) {
        if (voltage == Voltage.PRIMITIVE) {
            assert primitive != null;
            return primitive;
        }
        return machines.get(voltage);
    }

    public Block block(Voltage voltage) {
        if (voltage == Voltage.PRIMITIVE) {
            assert primitive != null;
            return primitive.get();
        }
        return machines.get(voltage).get();
    }

    public static abstract class BuilderBase<U extends MachineBlock<SmartBlockEntity>,
            T extends MachineSet<U>, P, S extends BuilderBase<U, T, P, S>>
            extends SimpleBuilder<T, P, S> {
        protected final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        protected Map<Voltage, Layout> layoutSet = null;

        protected BuilderBase(P parent) {
            super(parent);
        }

        public S voltage(Voltage from) {
            voltages.addAll(Voltage.between(from, Voltage.IV));
            return self();
        }

        public S voltage(Voltage from, Voltage to) {
            voltages.addAll(Voltage.between(from, to));
            return self();
        }

        public LayoutSetBuilder<S> layoutSet() {
            return Layout.builder(self()).onCreateObject(value -> layoutSet = value);
        }

        protected Layout getLayout(Voltage voltage) {
            assert layoutSet != null;
            return layoutSet.get(voltage);
        }

        protected abstract BlockEntityBuilder<SmartBlockEntity, U, ?>
        getMachineBuilder(Voltage voltage);

        @Nullable
        protected BlockEntityBuilder<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>, ?>
        getPrimitiveBuilder() {
            return null;
        }

        protected RegistryEntry<U>
        createMachine(Voltage voltage) {
            return getMachineBuilder(voltage).buildObject();
        }

        protected RegistryEntry<PrimitiveBlock<PrimitiveMachine>>
        createPrimitive() {
            var builder = getPrimitiveBuilder();
            assert builder != null;
            return builder.buildObject();
        }

        protected abstract T
        createSet(Set<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                  Map<Voltage, RegistryEntry<U>> machines,
                  @Nullable RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive);

        @Override
        protected T createObject() {
            assert layoutSet != null;
            if (voltages.isEmpty()) {
                voltage(Voltage.LV);
            }
            var machines = voltages.stream()
                    .filter(v -> v != Voltage.PRIMITIVE)
                    .collect(Collectors.toMap($ -> $, this::createMachine));
            var primitive = voltages.contains(Voltage.PRIMITIVE) ? createPrimitive() : null;
            return createSet(voltages, layoutSet, machines, primitive);
        }
    }

    public abstract static class Builder<U extends MachineBlock<SmartBlockEntity>, P>
            extends BuilderBase<U, MachineSet<U>, P, Builder<U, P>> {
        protected Builder(P parent) {
            super(parent);
        }

        @Override
        protected MachineSet<U>
        createSet(Set<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                  Map<Voltage, RegistryEntry<U>> machines,
                  @Nullable RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive) {
            return new MachineSet<>(voltages, layoutSet, machines, primitive);
        }
    }
}
