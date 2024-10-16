package org.shsts.tinactory.content.material;


import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COBALTITE;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class FirstDegrees {
    static {
        WROUGHT_IRON = set("wrought_iron")
                .color(0xFFC8B4B4)
                .metalSetExt().nugget()
                .toolSet(200, Tiers.IRON)
                .buildObject();

        BRONZE = set("bronze")
                .color(0xFFFF8000)
                .mechanicalSet().pipe().foil()
                .buildObject();

        COBALTITE = set("cobaltite")
                .color(0xFF5050FA)
                .dust()
                .buildObject();

        INVAR = set("invar")
                .color(0xFFB4B478)
                .metalSetExt()
                .buildObject();

        CUPRONICKEL = set("cupronickel")
                .color(0xFFE39680)
                .metalSet().wireAndPlate()
                .buildObject();

        STEEL = set("steel")
                .color(0xFF808080)
                .mechanicalSet().magnetic().pipe()
                .toolSet(800, Tiers.IRON)
                .buildObject();
    }

    public static void init() {}
}
