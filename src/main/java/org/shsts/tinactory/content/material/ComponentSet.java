package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ComponentSet {
    public final RegistryEntry<Item> motor;
    public final RegistryEntry<Item> pump;
    public final RegistryEntry<Item> piston;
    public final RegistryEntry<Item> conveyor;
    public final RegistryEntry<Item> robotArm;
    public final RegistryEntry<Item> sensor;
    public final RegistryEntry<Item> fieldGenerator;
    public final RegistryEntry<CableBlock> cable;

    private final List<Consumer<ComponentSet>> callbacks;

    public ComponentSet(SetBuilder<?> builder) {
        this.motor = builder.dummy("electric_motor");
        this.pump = builder.dummy("electric_pump");
        this.piston = builder.dummy("electric_piston");
        this.conveyor = builder.dummy("conveyor_module");
        this.robotArm = builder.dummy("robot_arm");
        this.sensor = builder.dummy("sensor");
        this.fieldGenerator = builder.dummy("field_generator");
        this.cable = builder.cableBlock();

        this.callbacks = builder.callbacks;
    }

    public void addRecipes() {
        for (var cb : callbacks) {
            cb.accept(this);
        }
        callbacks.clear();
    }

    public static class SetBuilder<P> extends SimpleBuilder<ComponentSet, P, SetBuilder<P>> {
        private final Voltage voltage;
        @Nullable
        private MaterialSet mainMaterial = null;
        @Nullable
        private MaterialSet cableMaterial = null;
        @Nullable
        private MaterialSet heatMaterial = null;
        private double cableResistance;

        private final List<Consumer<ComponentSet>> callbacks = new ArrayList<>();

        private SetBuilder(P parent, Voltage voltage) {
            super(parent);
            this.voltage = voltage;
        }

        public SetBuilder<P> cable(MaterialSet material, double resistance) {
            cableMaterial = material;
            cableResistance = resistance;
            return this;
        }

        public SetBuilder<P> material(MaterialSet main, MaterialSet heat) {
            mainMaterial = main;
            heatMaterial = heat;
            return this;
        }

        private RegistryEntry<Item> dummy(String name) {
            var id = "component/" + voltage.id + "/" + name;
            var texName = name.replace('_', '.') + "." + voltage.id;
            var texLoc = ModelGen.gregtech("items/metaitems/" + texName);
            return REGISTRATE.item(id, Item::new)
                    .model(ModelGen.basicItem(texLoc))
                    .register();
        }

        private RegistryEntry<CableBlock> cableBlock() {
            assert cableMaterial != null;
            var id = "network/cable/" + voltage.id;
            return REGISTRATE.block(id, CableBlock.cable(voltage, cableResistance))
                    .transform(ModelGen.cable())
                    .tint(CableBlock.INSULATION_COLOR, cableMaterial.color)
                    .tag(AllTags.MINEABLE_WITH_CUTTER)
                    .defaultBlockItem().dropSelf()
                    .register();
        }

        @Override
        protected ComponentSet createObject() {
            assert mainMaterial != null;
            assert heatMaterial != null;
            callbacks.add(set -> {
                ASSEMBLER.recipe(set.motor)
                        .outputItem(2, set.motor, 1)
                        .inputItem(0, mainMaterial.tag("stick"), 2)
                        .inputItem(0, heatMaterial.tag("wire"), 2 * voltage.rank)
                        .inputItem(0, set.cable, 2)
                        .workTicks(100)
                        .build();
            });
            return new ComponentSet(this);
        }
    }

    public static class Builder extends SimpleBuilder<Map<Voltage, ComponentSet>, Unit, Builder> {
        private final Map<Voltage, ComponentSet> sets = new HashMap<>();

        private Builder() {
            super(Unit.INSTANCE);
        }

        public SetBuilder<Builder> components(Voltage voltage) {
            var ret = new SetBuilder<>(this, voltage);
            ret.onCreateObject(set -> sets.put(voltage, set));
            return ret;
        }

        @Override
        protected Map<Voltage, ComponentSet> createObject() {
            return sets;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
