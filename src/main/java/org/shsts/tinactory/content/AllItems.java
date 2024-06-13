package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final RegistryEntry<CableBlock> ULV_CABLE;
    public static final RegistryEntry<Item> ULV_MACHINE_HULL;
    public static final RegistryEntry<Item> ULV_RESEARCH_EQUIPMENT;
    public static final Map<Voltage, ComponentSet> COMPONENT_SETS;
    public static final RegistryEntry<Item> VACUUM_TUBE;
    public static final RegistryEntry<SimpleFluid> STEAM;
    public static final RegistryEntry<Block> HEAT_PROOF_BLOCK;
    public static final RegistryEntry<BatteryItem> TEST_BATTERY;

    static {
        ULV_CABLE = REGISTRATE.block("network/cable/ulv",
                        prop -> new CableBlock(prop, CableBlock.WIRE_RADIUS, Voltage.ULV, 2.0))
                .tint(IRON.color)
                .register();

        ULV_MACHINE_HULL = REGISTRATE.item("component/ulv/machine_hull", Item::new)
                .register();

        ULV_RESEARCH_EQUIPMENT = REGISTRATE.item("component/ulv/research_equipment", Item::new)
                .tint(0xFFFFFFFF, IRON.color)
                .register();

        COMPONENT_SETS = ComponentSet.builder()
                .component(Voltage.LV, STEEL, TIN)
                .component(Voltage.MV, ALUMINIUM, COPPER)
                // TODO
                .component(Voltage.HV, STEEL, TIN)
                .component(Voltage.EV, STEEL, TIN)
                .component(Voltage.IV, STEEL, TIN)
                .buildObject();

        VACUUM_TUBE = REGISTRATE.item("circuit/vacuum_tube", Item::new).register();

        STEAM = REGISTRATE.simpleFluid("steam", gregtech("blocks/fluids/fluid.steam"));

        HEAT_PROOF_BLOCK = REGISTRATE.block("multi_block/solid/heat_proof", Block::new)
                .properties($ -> $.strength(2f, 8f))
                .register();

        TEST_BATTERY = REGISTRATE.item("battery/lv", prop -> new BatteryItem(prop, 12800))
                .register();
    }

    public static RegistryEntry<Item> researchEquipment(Voltage voltage) {
        return voltage == Voltage.ULV ? ULV_RESEARCH_EQUIPMENT :
                COMPONENT_SETS.get(voltage).researchEquipment;
    }

    public static void init() {}
}

