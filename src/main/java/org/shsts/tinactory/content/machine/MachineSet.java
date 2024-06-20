package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet {
    public final Set<Voltage> voltages;
    protected final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, RegistryEntry<? extends Block>> machines;

    public MachineSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                      Map<Voltage, RegistryEntry<? extends Block>> machines) {
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.voltages = new HashSet<>(voltages);
    }

    public RegistryEntry<? extends Block> entry(Voltage voltage) {
        return machines.get(voltage);
    }

    public Block block(Voltage voltage) {
        return machines.get(voltage).get();
    }

    public Layout layout(Voltage voltage) {
        return layoutSet.get(voltage);
    }

    public static abstract class BuilderBase<T extends MachineSet, P, S extends BuilderBase<T, P, S>>
            extends SimpleBuilder<T, P, S> {
        protected final List<Voltage> voltages = new ArrayList<>();
        @Nullable
        protected Map<Voltage, Layout> layoutSet = null;

        protected BuilderBase(P parent) {
            super(parent);
        }

        public S voltages(Voltage from) {
            voltages.addAll(Voltage.between(from, Voltage.IV));
            return self();
        }

        public S voltages(Voltage from, Voltage to) {
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

        protected abstract BlockEntityBuilder<SmartBlockEntity, ?, ?>
        getMachineBuilder(Voltage voltage);

        protected RegistryEntry<? extends Block>
        createMachine(Voltage voltage) {
            return getMachineBuilder(voltage).buildObject();
        }

        protected abstract T
        createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                  Map<Voltage, RegistryEntry<? extends Block>> machines);

        @Override
        protected T createObject() {
            assert layoutSet != null;
            if (voltages.isEmpty()) {
                voltages(Voltage.LV);
            }
            var machines = new HashMap<Voltage, RegistryEntry<? extends Block>>();
            voltages.forEach(v -> machines.put(v, createMachine(v)));
            return createSet(voltages, layoutSet, machines);
        }
    }

    public abstract static class Builder<P>
            extends BuilderBase<MachineSet, P, Builder<P>> {
        protected Builder(P parent) {
            super(parent);
        }

        @Override
        protected MachineSet createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                                       Map<Voltage, RegistryEntry<? extends Block>> machines) {
            return new MachineSet(voltages, layoutSet, machines);
        }
    }
}
