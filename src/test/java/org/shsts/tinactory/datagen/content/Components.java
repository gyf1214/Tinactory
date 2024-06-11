package org.shsts.tinactory.datagen.content;


import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllItems.COMPONENT_SETS;
import static org.shsts.tinactory.content.AllItems.HEAT_PROOF_BLOCK;
import static org.shsts.tinactory.content.AllItems.ULV_CABLE;
import static org.shsts.tinactory.content.AllItems.ULV_MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.ULV_RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.machineItem;
import static org.shsts.tinactory.datagen.content.Models.solidBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    private static final String MACHINE_HULL_TEX = "overlay/machine/overlay_energy_out";
    private static final String RESEARCH_TEX = "metaitems/glass_vial/";

    public static void init() {
        DATA_GEN.block(ULV_CABLE)
                .blockState(Models::ulvCableBlock)
                .itemModel(Models::ulvCableItem)
                .tag(MINEABLE_WITH_CUTTER)
                .build()
                .item(ULV_MACHINE_HULL)
                .model(machineItem(Voltage.ULV, MACHINE_HULL_TEX))
                .build()
                .item(ULV_RESEARCH_EQUIPMENT)
                .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
                .build()
                .item(VACUUM_TUBE)
                .model(basicItem("metaitems/circuit.vacuum_tube"))
                .build()
                .block(HEAT_PROOF_BLOCK)
                .blockState(solidBlock("casings/solid/machine_casing_heatproof"))
                .tag(MINEABLE_WITH_WRENCH)
                .build();

        for (var entry : COMPONENT_SETS.entrySet()) {
            componentItems(entry.getKey(), entry.getValue());
        }
    }

    private static void componentItems(Voltage voltage, ComponentSet set) {
        for (var entry : set.dummyItems) {
            var names = entry.id.split("/");
            var name = names[names.length - 1];

            var tex = "metaitems/" + name.replace('_', '.') + "." + voltage.id;
            DATA_GEN.item(entry)
                    .model(basicItem(tex))
                    .build();
        }

        DATA_GEN.item(set.machineHull)
                .model(machineItem(voltage, MACHINE_HULL_TEX))
                .build()
                .block(set.cable)
                .blockState(Models::cableBlock)
                .itemModel(Models::cableItem)
                .tag(MINEABLE_WITH_CUTTER)
                .build()
                .item(set.researchEquipment)
                .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
                .build();
    }
}
