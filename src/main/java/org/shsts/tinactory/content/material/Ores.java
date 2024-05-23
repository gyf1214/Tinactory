package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Ores {
    static {
        CHALCOPYRITE = set("chalcopyrite")
                .color(0xFFA07828).icon(IconSet.DULL)
                .smelt(ingot("copper"))
                .stoneOre()
                .primitive()
                // TODO: pyrite, cobalt, cadmium
                .byproduct(dust("pyrite"), dust("pyrite"), dust("pyrite"))
                .build().buildObject();

        PYRITE = set("pyrite")
                .color(0xFF967828).icon(IconSet.ROUGH)
                .smelt(ingot("iron"))
                .stoneOre()
                .primitive()
                // TODO: sulfur, tricalcium
                .byproduct(dust("pyrite"), dust("pyrite"))
                .build().buildObject();

        MAGNETITE = set("magnetite")
                .color(0xFF1E1E1E).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .stoneOre()
                .byproduct(dust("magnetite"), dust("gold"))
                .build()
                .buildObject();

        CASSITERITE = set("cassiterite")
                .color(0xFFDCDCDC).icon(IconSet.METALLIC)
                .smelt(ingot("tin"))
                .stoneOre()
                .amount(2)
                // TODO: tin, zinc
                .byproduct(dust("tin"), dust("tin"))
                .build().createObject();
    }

    public static void init() {}
}
