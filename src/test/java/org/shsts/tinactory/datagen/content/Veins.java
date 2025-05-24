package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.datagen.content.builder.VeinBuilder;

import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.BAUXITE;
import static org.shsts.tinactory.content.AllMaterials.BLUE_TOPAZ;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.EMERALD;
import static org.shsts.tinactory.content.AllMaterials.GALENA;
import static org.shsts.tinactory.content.AllMaterials.GARNIERITE;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.GRAPHITE;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_OIL;
import static org.shsts.tinactory.content.AllMaterials.ILMENITE;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_OIL;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.SAPPHIRE;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SPHALERITE;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.TOPAZ;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Veins {
    public static void init() {
        VEINS.vein("chalcopyrite", 0.4)
            .primitive()
            .ore(CHALCOPYRITE, 0.8)
            .ore(PYRITE, 0.6)
            .build()
            .vein("limonite", 0.6)
            .base()
            .ore(LIMONITE, 0.8)
            .ore(BANDED_IRON, 0.4)
            .ore(GARNIERITE, 0.2)
            .build()
            .vein("coal", 0.3)
            .ore(COAL, 1)
            .ore(COAL, 0.4)
            .build()
            .vein("cassiterite", 0.2)
            .ore(TIN, 1)
            .ore(CASSITERITE, 0.3)
            .ore(TIN, 0.1)
            .build()
            .vein("redstone", 0.1)
            .ore(REDSTONE, 1)
            .ore(RUBY, 0.3)
            .ore(CINNABAR, 0.1)
            .build()
            .vein("magnetite", 0.4)
            .ore(MAGNETITE, 1)
            .ore(GOLD, 0.2)
            .ore(MAGNETITE, 0.2)
            .build()
            .vein("sulfide", 0.4)
            .ore(GALENA, 0.5)
            .ore(SPHALERITE, 0.6)
            .ore(SILVER, 0.3)
            .build()
            .vein("graphite", 0.2)
            .ore(GRAPHITE, 1)
            .ore(GRAPHITE, 0.3)
            .ore(DIAMOND, 0.1)
            .build()
            .vein("bauxite", 0.5)
            .ore(BAUXITE, 0.8)
            .ore(ILMENITE, 0.6)
            .build()
            .vein("oil", 0.3)
            .ore(NATURAL_GAS, 0.3)
            .ore(LIGHT_OIL, 0.4)
            .ore(HEAVY_OIL, 0.7)
            .build()
            .vein("gem", 0.1)
            .ore(EMERALD, 0.4)
            .ore(SAPPHIRE, 0.7)
            .build()
            .vein("gold", 0.2)
            .variant(OreVariant.NETHERRACK)
            .ore(GOLD, 0.5)
            .ore(TOPAZ, 0.3)
            .ore(BLUE_TOPAZ, 0.3)
            .build();
    }

    private static class VeinFactory {
        private int rank = 0;

        public VeinBuilder<VeinFactory> vein(String id, double rate) {
            return VeinBuilder.factory(this, id, rank++, rate);
        }
    }

    private static final VeinFactory VEINS = new VeinFactory();
}
