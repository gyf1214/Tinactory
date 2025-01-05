package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.AIR;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.liquid;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class HigherDegrees {
    static {
        COBALT_BRASS = set("cobalt_brass")
            .color(0xFFB4B4A0)
            .gear()
            .toolSet(1000, Tiers.IRON)
            .buildObject();

        AIR = set("air")
            .color(0xFFFFFFFF)
            .fluid("gas", "air", 1000)
            .fluid("liquid", "liquid_air", 1000)
            .fluidPrimary("gas")
            .buildObject();

        SALT_WATER = liquid("salt_water", 0xFF0000C8);
    }

    public static void init() {}
}
