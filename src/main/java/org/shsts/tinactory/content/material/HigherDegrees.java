package org.shsts.tinactory.content.material;

import net.minecraft.world.item.Tiers;

import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.set;

public final class HigherDegrees {
    static {
        COBALT_BRASS = set("cobalt_brass")
            .color(0xFFB4B4A0)
            .gear()
            .toolSet(1000, Tiers.IRON)
            .buildObject();
    }

    public static void init() {}
}
