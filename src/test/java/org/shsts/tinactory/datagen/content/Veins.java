package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.datagen.content.builder.VeinBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.GARNIERITE;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

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
                .vein("magnetite", 0.8)
                .ore(MAGNETITE, 1)
                .ore(GOLD, 0.2)
                .ore(MAGNETITE, 0.2)
                .build();
    }

    private static class VeinFactory {
        public VeinBuilder<VeinFactory> vein(String id, double rate) {
            return new VeinBuilder<>(DATA_GEN, this, id, rate);
        }
    }

    private static final VeinFactory VEINS = new VeinFactory();
}
