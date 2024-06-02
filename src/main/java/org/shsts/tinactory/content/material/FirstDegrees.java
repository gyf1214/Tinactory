package org.shsts.tinactory.content.material;


import net.minecraft.world.item.Tiers;
import org.shsts.tinactory.content.machine.Voltage;

import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class FirstDegrees {
    static {
        WROUGHT_IRON = set("wrought_iron")
                .color(0xFFC8B4B4).icon(IconSet.METALLIC)
                .metalSetExt()
                .toolProcess().smelt()
                .tool(200, Tiers.IRON).basic().build()
                .buildObject();

        BRONZE = set("bronze")
                .color(0xFFFF8000).icon(IconSet.METALLIC)
                .mechanicalSet().pipe()
                .alloy(Voltage.ULV, COPPER, 3, TIN, 1)
                .smelt()
                .buildObject();

        INVAR = set("invar")
                .color(0xFFB4B478).icon(IconSet.METALLIC)
                .metalSetExt()
                .toolProcess()
                .alloy(Voltage.ULV, IRON, 2, NICKEL, 1)
                .buildObject();

        CUPRONICKEL = set("cupronickel")
                .color(0xFFE39680).icon(IconSet.METALLIC)
                .metalSet().wireAndPlate()
                .toolProcess()
                .alloy(Voltage.ULV, COPPER, 1, NICKEL, 1)
                .buildObject();

        STEEL = set("steel")
                .color(0xFF808080).icon(IconSet.METALLIC)
                .mechanicalSet().magnetic().pipe()
                .toolProcess()
                .tool(800, Tiers.IRON).basic().build()
                .buildObject();
    }

    public static void init() {}
}
