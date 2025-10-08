package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet {
    public final Set<Voltage> voltages;
    protected final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, ? extends IEntry<? extends Block>> machines;

    public MachineSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
        Map<Voltage, ? extends IEntry<? extends Block>> machines) {
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.voltages = new HashSet<>(voltages);
    }

    public MachineSet(Map<Voltage, Layout> layoutSet,
        Map<Voltage, ? extends IEntry<? extends Block>> machines) {
        this(machines.keySet(), layoutSet, machines);
    }

    public boolean hasVoltage(Voltage voltage) {
        return machines.containsKey(voltage);
    }

    public IEntry<? extends Block> entry(Voltage voltage) {
        return machines.get(voltage);
    }

    public Block block(Voltage voltage) {
        return machines.get(voltage).get();
    }

    public Layout layout(Voltage voltage) {
        return layoutSet.get(voltage);
    }

    public static class Builder<P> extends SimpleBuilder<MachineSet, P, Builder<P>> {
        protected final IRegistrate registrate;
        protected final List<Voltage> voltages = new ArrayList<>();
        @Nullable
        protected Function<Voltage, BlockEntityBuilder<?, ?>> blockEntityBuilder = null;
        @Nullable
        protected LayoutSetBuilder<Builder<P>> layoutSetBuilder = null;
        protected Map<Voltage, Layout> layoutSet = Layout.EMPTY_SET;

        public Builder(IRegistrate registrate, P parent) {
            super(parent);
            this.registrate = registrate;
        }

        public Builder<P> voltages(Voltage from) {
            voltages.addAll(Voltage.between(from, Voltage.IV));
            return self();
        }

        public Builder<P> voltages(Voltage from, Voltage to) {
            voltages.addAll(Voltage.between(from, to));
            return self();
        }

        public LayoutSetBuilder<Builder<P>> layoutSet() {
            if (layoutSetBuilder == null) {
                layoutSetBuilder = Layout.builder(self())
                    .onCreateObject($ -> layoutSet = $);
            }
            return layoutSetBuilder;
        }

        public Layout getLayout(Voltage voltage) {
            return layoutSet.get(voltage);
        }

        public Builder<P> machine(Function<Voltage, String> id,
            Function<Voltage, SmartEntityBlock.Factory<?>> blockFactory) {
            assert blockEntityBuilder == null;
            blockEntityBuilder = v -> BlockEntityBuilder.builder(
                id.apply(v), blockFactory.apply(v));
            return self();
        }

        public Builder<P> menu(IMenuType menu) {
            assert blockEntityBuilder != null;
            var old = blockEntityBuilder;
            blockEntityBuilder = v -> old.apply(v).menu(menu);
            return self();
        }

        public <V> Builder<P> machine(Transformer<IBlockEntityTypeBuilder<V>> trans) {
            assert blockEntityBuilder != null;
            var old = blockEntityBuilder;
            blockEntityBuilder = v -> old.apply(v)
                .blockEntity().transform(trans.cast()).end();
            return self();
        }

        public <V> Builder<P> layoutMachine(
            Function<Layout, Transformer<IBlockEntityTypeBuilder<V>>> trans) {
            assert blockEntityBuilder != null;
            var old = blockEntityBuilder;
            blockEntityBuilder = v -> old.apply(v)
                .blockEntity().transform(trans.apply(getLayout(v)).cast()).end();
            return self();
        }

        public <U extends Block, V> Builder<P> voltageBlock(
            Function<Voltage, Transformer<IBlockBuilder<U, V>>> trans) {
            assert blockEntityBuilder != null;
            var old = blockEntityBuilder;
            blockEntityBuilder = v -> old.apply(v)
                .block().transform(trans.apply(v).cast()).end();
            return self();
        }

        public Builder<P> tintVoltage(int index) {
            return voltageBlock(v -> $ -> $.tint(i -> i == index ? v.color : 0xFFFFFFFF));
        }

        protected IEntry<? extends Block> createMachine(Voltage voltage) {
            assert blockEntityBuilder != null;
            return blockEntityBuilder.apply(voltage)
                .transform(MachineSet::baseMachine)
                .buildObject();
        }

        protected MachineSet createSet(Collection<Voltage> voltages,
            Map<Voltage, Layout> layoutSet,
            Map<Voltage, IEntry<? extends Block>> machines) {
            return new MachineSet(voltages, layoutSet, machines);
        }

        @Override
        protected MachineSet createObject() {
            if (voltages.isEmpty()) {
                voltages(Voltage.LV);
            }
            var machines = new HashMap<Voltage, IEntry<? extends Block>>();
            voltages.forEach(v -> machines.put(v, createMachine(v)));
            return createSet(voltages, layoutSet, machines);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(REGISTRATE, parent);
    }

    public static <U extends SmartEntityBlock, P> BlockEntityBuilder<U, P> baseMachine(
        BlockEntityBuilder<U, P> builder) {
        return builder.blockEntity()
            .transform(Machine::factory)
            .end()
            .block()
            .material(Material.HEAVY_METAL)
            .properties(MACHINE_PROPERTY)
            .translucent()
            .end();
    }
}
