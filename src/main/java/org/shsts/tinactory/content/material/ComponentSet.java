package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.registrate.builder.ItemBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

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
    public final RegistryEntry<Item> machineHull;
    public final RegistryEntry<CableBlock> cable;
    public final RegistryEntry<Item> researchEquipment;

    public final MaterialSet mainMaterial;
    public final MaterialSet cableMaterial;
    public final List<RegistryEntry<Item>> dummyItems;

    private ItemBuilder<Item, ?, ?> item(Voltage voltage, String name) {
        var id = "component/" + voltage.id + "/" + name;
        return REGISTRATE.item(id, Item::new);
    }

    private RegistryEntry<Item> dummy(Voltage voltage, String name) {
        var ret = item(voltage, name).register();
        dummyItems.add(ret);
        return ret;
    }

    public ComponentSet(Voltage voltage, MaterialSet mainMaterial, MaterialSet cableMaterial) {
        this.dummyItems = new ArrayList<>();
        this.mainMaterial = mainMaterial;
        this.cableMaterial = cableMaterial;

        this.motor = dummy(voltage, "electric_motor");
        this.pump = dummy(voltage, "electric_pump");
        this.piston = dummy(voltage, "electric_piston");
        this.conveyor = dummy(voltage, "conveyor_module");
        this.robotArm = dummy(voltage, "robot_arm");
        this.sensor = dummy(voltage, "sensor");
        this.fieldGenerator = dummy(voltage, "field_generator");

        this.machineHull = item(voltage, "machine_hull").register();

        this.cable = REGISTRATE.block("network/cable/" + voltage.id, CableBlock.cable(voltage, 1d))
                .translucent()
                .tint(CableBlock.INSULATION_COLOR, cableMaterial.color)
                .register();

        this.researchEquipment = item(voltage, "research_equipment")
                .tint(0xFFFFFFFF, mainMaterial.color)
                .register();
    }

    // TODO
//    public static class SetBuilder<P> extends SimpleBuilder<ComponentSet, P, SetBuilder<P>> {
//        private static final int ASSEMBLE_TICKS = 100;
//
//        private final Voltage voltage;
//        @Nullable
//        private MaterialSet mainMaterial = null;
//        @Nullable
//        private MaterialSet heatMaterial = null;
//        @Nullable
//        private MaterialSet pipeMaterial = null;
//        @Nullable
//        private MaterialSet rotorMaterial = null;
//        @Nullable
//        private MaterialSet cableMaterial = null;
//        @Nullable
//        private MaterialSet magneticMaterial = null;
//
//        private final List<Consumer<ComponentSet>> callbacks = new ArrayList<>();
//
//        private SetBuilder(P parent, Voltage voltage) {
//            super(parent);
//            this.voltage = voltage;
//        }
//
//        public SetBuilder<P> cable(MaterialSet material) {
//            cableMaterial = material;
//            return this;
//        }
//
//        public SetBuilder<P> material(MaterialSet val) {
//            mainMaterial = val;
//            return this;
//        }
//
//        public SetBuilder<P> heat(MaterialSet val) {
//            heatMaterial = val;
//            return this;
//        }
//
//        public SetBuilder<P> pipe(MaterialSet val) {
//            pipeMaterial = val;
//            return this;
//        }
//
//        public SetBuilder<P> rotor(MaterialSet val) {
//            rotorMaterial = val;
//            return this;
//        }
//
//        public SetBuilder<P> magnetic(MaterialSet val) {
//            magneticMaterial = val;
//            return this;
//        }
//
//        @Override
//        protected ComponentSet createObject() {
//            assert mainMaterial != null;
//            assert heatMaterial != null;
//            assert magneticMaterial != null;
//            assert pipeMaterial != null;
//            assert rotorMaterial != null;
//            callbacks.add(set -> {
//                var assembleVoltage = this.voltage == Voltage.LV ? Voltage.ULV : Voltage.LV;
//
//                ASSEMBLER.recipe(set.motor)
//                        .outputItem(2, set.motor, 1)
//                        .inputItem(0, magneticMaterial.tag("magnetic"), 1)
//                        .inputItem(0, mainMaterial.tag("stick"), 2)
//                        .inputItem(0, heatMaterial.tag("wire"), 2 * voltage.rank)
//                        .inputItem(0, set.cable, 2)
//                        .workTicks(ASSEMBLE_TICKS)
//                        .voltage(assembleVoltage)
//                        .build();
//
//                ASSEMBLER.recipe(set.pump)
//                        .outputItem(2, set.pump, 1)
//                        .inputItem(0, set.motor, 1)
//                        .inputItem(0, pipeMaterial.tag("pipe"), 1)
//                        .inputItem(0, rotorMaterial.tag("rotor"), 1)
//                        .inputItem(0, rotorMaterial.tag("screw"), 3)
//                        // TODO rubber seal
//                        .inputItem(0, set.cable, 1)
//                        .workTicks(ASSEMBLE_TICKS)
//                        .voltage(assembleVoltage)
//                        .build();
//
//                ASSEMBLER.recipe(set.piston)
//                        .outputItem(2, set.piston, 1)
//                        .inputItem(0, set.motor, 1)
//                        .inputItem(0, mainMaterial.tag("plate"), 3)
//                        .inputItem(0, mainMaterial.tag("stick"), 2)
//                        .inputItem(0, mainMaterial.tag("gear"), 1)
//                        .inputItem(0, set.cable, 2)
//                        .workTicks(ASSEMBLE_TICKS)
//                        .voltage(assembleVoltage)
//                        .build();
//
//                ASSEMBLER.recipe(set.machineHull)
//                        .outputItem(2, set.machineHull, 1)
//                        .inputItem(0, mainMaterial.tag("plate"), 8)
//                        .inputItem(0, set.cable, 2)
//                        .workTicks(ASSEMBLE_TICKS)
//                        .voltage(assembleVoltage)
//                        .build();
//            });
//            return new ComponentSet(this);
//        }
//    }

    public static class Builder extends SimpleBuilder<Map<Voltage, ComponentSet>, Unit, Builder> {
        private final Map<Voltage, ComponentSet> components = new HashMap<>();

        private Builder() {
            super(Unit.INSTANCE);
        }

        public Builder component(Voltage voltage, MaterialSet mainMaterial,
                                 MaterialSet cableMaterial) {
            components.put(voltage, new ComponentSet(voltage, mainMaterial, cableMaterial));
            return this;
        }

        @Override
        protected Map<Voltage, ComponentSet> createObject() {
            return components;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
