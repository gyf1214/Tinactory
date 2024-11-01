package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    public boolean hasVoltage(Voltage voltage) {
        return machines.containsKey(voltage);
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

    public Block icon() {
        var voltage = voltages.stream()
                .filter(v -> v != Voltage.ULV)
                .min(Comparator.comparingInt(v -> v.rank))
                .orElseThrow();
        return machines.get(voltage).get();
    }

    public static abstract class BuilderBase<T extends MachineSet, P, S extends BuilderBase<T, P, S>>
            extends SimpleBuilder<T, P, S> {
        protected final Registrate registrate;
        protected final List<Voltage> voltages = new ArrayList<>();
        @Nullable
        protected BiFunction<Voltage, S, BlockEntityBuilder<SmartBlockEntity, ?, ?>> blockEntityBuilder = null;
        @Nullable
        protected LayoutSetBuilder<S> layoutSetBuilder = null;
        @Nullable
        protected Map<Voltage, Layout> layoutSet = null;

        protected BuilderBase(Registrate registrate, P parent) {
            super(parent);
            this.registrate = registrate;
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
            if (layoutSetBuilder == null) {
                layoutSetBuilder = Layout.builder(self());
            }
            return layoutSetBuilder;
        }

        public Layout getLayout(Voltage voltage) {
            assert layoutSet != null;
            return layoutSet.get(voltage);
        }

        public S machine(Function<Voltage, String> id,
                         Function<Voltage, EntityBlockBuilder.Factory<SmartBlockEntity, ?>> blockFactory) {
            assert blockEntityBuilder == null;
            blockEntityBuilder = (v, $) -> registrate.blockEntity(id.apply(v), blockFactory.apply(v));
            return self();
        }

        public <V extends BlockEntityBuilder<SmartBlockEntity, ?, ?>>
        S machine(BiFunction<Voltage, S, Transformer<V>> trans) {
            assert blockEntityBuilder != null;
            var old = blockEntityBuilder;
            blockEntityBuilder = (v, $) -> old.apply(v, $)
                    .transform(trans.apply(v, $).cast());
            return self();
        }

        public <V extends BlockEntityBuilder<SmartBlockEntity, ?, ?>>
        S machine(Function<Voltage, Transformer<V>> trans) {
            return machine((v, $) -> trans.apply(v));
        }


        @SuppressWarnings("unchecked")
        protected static <B> Function<B, CapabilityProviderBuilder<? super SmartBlockEntity, B>>
        cast(Function<?, ? extends CapabilityProviderBuilder<? super SmartBlockEntity, ?>> from) {
            return (Function<B, CapabilityProviderBuilder<? super SmartBlockEntity, B>>) from;
        }

        public <B> S capability(Function<B, ? extends
                CapabilityProviderBuilder<? super SmartBlockEntity, B>> factory) {
            return machine(v -> $ -> $.blockEntity()
                    .simpleCapability(cast(factory))
                    .build());
        }

        public <B> S layoutCapability(Function<Layout, Function<B, ? extends
                CapabilityProviderBuilder<? super SmartBlockEntity, B>>> factory) {
            return machine(v -> $ -> $.blockEntity()
                    .simpleCapability(cast(factory.apply(getLayout(v))))
                    .build());
        }

        public S layoutMenu(Function<Layout, Menu.Factory<SmartBlockEntity, ?>> factory) {
            return machine(v -> $ -> $.blockEntity()
                    .menu(factory.apply(getLayout(v))).build()
                    .build());
        }

        public S plugin(IMenuPlugin.Factory<?> factory) {
            return machine(v -> $ -> $.blockEntity()
                    .menu().plugin(factory).build()
                    .build());
        }

        public S layoutPlugin(Function<Layout, IMenuPlugin.Factory<?>> factory) {
            return machine(v -> $ -> $.blockEntity()
                    .menu().plugin(factory.apply(getLayout(v))).build()
                    .build());
        }

        protected RegistryEntry<? extends Block>
        createMachine(Voltage voltage) {
            assert blockEntityBuilder != null;
            return blockEntityBuilder.apply(voltage, self())
                    .transform(baseMachine())
                    .buildObject();
        }

        protected abstract T
        createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                  Map<Voltage, RegistryEntry<? extends Block>> machines);

        @Override
        protected T createObject() {
            assert layoutSetBuilder != null;
            layoutSet = layoutSetBuilder.buildObject();
            if (voltages.isEmpty()) {
                voltages(Voltage.LV);
            }
            var machines = new HashMap<Voltage, RegistryEntry<? extends Block>>();
            voltages.forEach(v -> machines.put(v, createMachine(v)));
            return createSet(voltages, layoutSet, machines);
        }
    }

    public static class Builder<P> extends BuilderBase<MachineSet, P, Builder<P>> {
        public Builder(Registrate registrate, P parent) {
            super(registrate, parent);
        }

        @Override
        protected MachineSet createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                                       Map<Voltage, RegistryEntry<? extends Block>> machines) {
            return new MachineSet(voltages, layoutSet, machines);
        }
    }

    public static <T extends SmartBlockEntity, U extends SmartEntityBlock<T>, P>
    Transformer<BlockEntityBuilder<T, U, P>> baseMachine() {
        return $ -> $.blockEntity()
                .eventManager()
                .simpleCapability(Machine::builder)
                .menu().title(ProcessingMenu::getTitle).build()
                .build()
                .translucent();
    }
}
