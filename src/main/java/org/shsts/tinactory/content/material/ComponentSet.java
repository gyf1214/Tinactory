package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.MachineModel;
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
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

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
    public final RegistryEntry<Item> machineHull;
    public final RegistryEntry<Item> researchEquipment;

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
        this.machineHull = builder.machineHull();
        this.researchEquipment = builder.researchEquipment();

        this.callbacks = builder.callbacks;
    }

    public void addRecipes() {
        for (var cb : callbacks) {
            cb.accept(this);
        }
        callbacks.clear();
    }

    public static class SetBuilder<P> extends SimpleBuilder<ComponentSet, P, SetBuilder<P>> {
        private static final int ASSEMBLE_TICKS = 100;

        private final Voltage voltage;
        @Nullable
        private MaterialSet mainMaterial = null;
        @Nullable
        private MaterialSet heatMaterial = null;
        @Nullable
        private MaterialSet pipeMaterial = null;
        @Nullable
        private MaterialSet rotorMaterial = null;
        @Nullable
        private MaterialSet cableMaterial = null;
        @Nullable
        private MaterialSet magneticMaterial = null;

        private final List<Consumer<ComponentSet>> callbacks = new ArrayList<>();

        private SetBuilder(P parent, Voltage voltage) {
            super(parent);
            this.voltage = voltage;
        }

        public SetBuilder<P> cable(MaterialSet material) {
            cableMaterial = material;
            return this;
        }

        public SetBuilder<P> material(MaterialSet val) {
            mainMaterial = val;
            return this;
        }

        public SetBuilder<P> heat(MaterialSet val) {
            heatMaterial = val;
            return this;
        }

        public SetBuilder<P> pipe(MaterialSet val) {
            pipeMaterial = val;
            return this;
        }

        public SetBuilder<P> rotor(MaterialSet val) {
            rotorMaterial = val;
            return this;
        }

        public SetBuilder<P> magnetic(MaterialSet val) {
            magneticMaterial = val;
            return this;
        }

        private RegistryEntry<Item> dummy(String name) {
            var id = "component/" + voltage.id + "/" + name;
            var texName = name.replace('_', '.') + "." + voltage.id;
            var texLoc = gregtech("items/metaitems/" + texName);
            return REGISTRATE.item(id, Item::new)
                    .model(ModelGen.basicItem(texLoc))
                    .register();
        }

        private RegistryEntry<Item> machineHull() {
            return REGISTRATE.item("component/" + voltage.id + "/machine_hull", Item::new)
                    .model(ModelGen.machineItem(voltage, gregtech(MachineModel.IO_TEX)))
                    .register();
        }

        private RegistryEntry<CableBlock> cableBlock() {
            assert cableMaterial != null;
            var id = "network/cable/" + voltage.id;
            return REGISTRATE.block(id, CableBlock.cable(voltage, 1d))
                    .transform(ModelGen.cable())
                    .tint(CableBlock.INSULATION_COLOR, cableMaterial.color)
                    .tag(AllTags.MINEABLE_WITH_CUTTER)
                    .register();
        }

        private RegistryEntry<Item> researchEquipment() {
            assert mainMaterial != null;
            return REGISTRATE.item("component/" + voltage.id + "/research_equipment", Item::new)
                    .model(ModelGen.basicItem(gregtech("items/metaitems/glass_vial/base"),
                            gregtech("items/metaitems/glass_vial/overlay")))
                    .tint(0xFFFFFFFF, mainMaterial.color)
                    .register();
        }

        @Override
        protected ComponentSet createObject() {
            assert mainMaterial != null;
            assert heatMaterial != null;
            assert magneticMaterial != null;
            assert pipeMaterial != null;
            assert rotorMaterial != null;
            callbacks.add(set -> {
                var assembleVoltage = this.voltage == Voltage.LV ? Voltage.ULV : Voltage.LV;

                ASSEMBLER.recipe(set.motor)
                        .outputItem(2, set.motor, 1)
                        .inputItem(0, magneticMaterial.tag("magnetic"), 1)
                        .inputItem(0, mainMaterial.tag("stick"), 2)
                        .inputItem(0, heatMaterial.tag("wire"), 2 * voltage.rank)
                        .inputItem(0, set.cable, 2)
                        .workTicks(ASSEMBLE_TICKS)
                        .voltage(assembleVoltage)
                        .build();

                ASSEMBLER.recipe(set.pump)
                        .outputItem(2, set.pump, 1)
                        .inputItem(0, set.motor, 1)
                        .inputItem(0, pipeMaterial.tag("pipe"), 1)
                        .inputItem(0, rotorMaterial.tag("rotor"), 1)
                        .inputItem(0, rotorMaterial.tag("screw"), 3)
                        // TODO rubber seal
                        .inputItem(0, set.cable, 1)
                        .workTicks(ASSEMBLE_TICKS)
                        .voltage(assembleVoltage)
                        .build();

                ASSEMBLER.recipe(set.piston)
                        .outputItem(2, set.piston, 1)
                        .inputItem(0, set.motor, 1)
                        .inputItem(0, mainMaterial.tag("plate"), 3)
                        .inputItem(0, mainMaterial.tag("stick"), 2)
                        .inputItem(0, mainMaterial.tag("gear"), 1)
                        .inputItem(0, set.cable, 2)
                        .workTicks(ASSEMBLE_TICKS)
                        .voltage(assembleVoltage)
                        .build();

                ASSEMBLER.recipe(set.machineHull)
                        .outputItem(2, set.machineHull, 1)
                        .inputItem(0, mainMaterial.tag("plate"), 8)
                        .inputItem(0, set.cable, 2)
                        .workTicks(ASSEMBLE_TICKS)
                        .voltage(assembleVoltage)
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
