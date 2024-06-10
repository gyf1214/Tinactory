package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllTechs.BASE_ORE;
import static org.shsts.tinactory.datagen.DataGen.REGISTRATE;
import static org.shsts.tinactory.datagen.content.RecipeTypes.ORE_ANALYZER;
import static org.shsts.tinactory.datagen.content.RecipeTypes.RESEARCH;

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
                .ore(LIMONITE, 1)
                .ore(BANDED_IRON, 0.4)
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
                .build();
    }

    private static class VeinFactory {
        public VeinBuilder vein(String id, double rate) {
            return new VeinBuilder(this, id, rate);
        }
    }

    private static final VeinFactory VEINS = new VeinFactory();

    private static class VeinBuilder extends SimpleBuilder<Unit, VeinFactory, VeinBuilder> {
        private final String id;
        private final double rate;
        private final OreAnalyzerRecipe.Builder builder;
        private final List<MaterialSet> ores = new ArrayList<>();
        private boolean baseOre = false;
        private boolean primitive = false;
        private OreVariant variant = null;

        public VeinBuilder(VeinFactory parent, String id, double rate) {
            super(parent);
            this.id = id;
            this.rate = rate;
            this.builder = ORE_ANALYZER.recipe(id).rate(rate);
        }

        public VeinBuilder ore(MaterialSet material, double rate) {
            builder.outputItem(ores.size() + 1, material.entry("raw"), 1, rate);
            ores.add(material);
            if (variant == null) {
                variant = material.oreVariant();
                builder.inputOre(variant);
            }
            assert variant == material.oreVariant();
            return this;
        }

        public VeinBuilder primitive() {
            builder.primitive();
            primitive = true;
            baseOre = true;
            return this;
        }

        public VeinBuilder base() {
            baseOre = true;
            return this;
        }

        @Override
        protected Unit createObject() {
            assert variant != null;
            assert rate > 0d;
            assert !ores.isEmpty();
            if (!primitive) {
                builder.voltage(variant.voltage);
            }
            var tech = BASE_ORE.get(variant);
            var baseProgress = 50L * (1L << (long) variant.rank);
            if (!baseOre) {
                tech = REGISTRATE.tech("ore/" + id)
                        .maxProgress(baseProgress)
                        .displayItem(ores.get(0).loc("raw"))
                        .depends(tech).buildObject();

                RESEARCH.recipe(tech)
                        .target(tech)
                        .defaultInput(variant.voltage)
                        .build();
            }

            for (var ore : new HashSet<>(ores)) {
                RESEARCH.recipe(tech.getPath() + "_from_" + ore.name)
                        .target(tech)
                        .inputItem(ore.tag("raw"))
                        .voltage(variant.voltage)
                        .build();
            }

            if (!primitive) {
                builder.requireTech(tech);
            }
            builder.build();
            return Unit.INSTANCE;
        }
    }
}
