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
