package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntitySetBuilder;
import org.shsts.tinactory.registrate.common.BlockEntitySet;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet {
    public final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines;
    @Nullable
    protected final BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive;

    public MachineSet(Map<Voltage, Layout> layoutSet,
                      Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines,
                      @Nullable BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive) {
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.primitive = primitive;
    }

    public static abstract class Builder<T extends MachineSet, P, S extends Builder<T, P, S>>
            extends SimpleBuilder<T, P, S> {
        protected final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        protected Map<Voltage, Layout> layoutSet = null;

        protected Builder(P parent) {
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

        protected abstract BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
        getMachineBuilder(Voltage voltage);

        protected abstract BlockEntitySetBuilder<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
        getPrimitiveBuilder();

        protected BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
        createMachine(Voltage voltage) {
            return getMachineBuilder(voltage).register();
        }

        protected BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
        createPrimitive() {
            return getPrimitiveBuilder().register();
        }

        protected abstract T
        createSet(Map<Voltage, Layout> layoutSet,
                  Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines,
                  @Nullable BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive);

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
            return createSet(layoutSet, machines, primitive);
        }
    }
}
