package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTechs;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.dust;
import static org.shsts.tinactory.content.AllMaterials.ingot;
import static org.shsts.tinactory.content.AllMaterials.set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Ores {
    static {
        CHALCOPYRITE = set("chalcopyrite")
                .color(0xFFA07828).icon(IconSet.DULL)
                .smelt(ingot("copper"))
                .ore(OreVariant.STONE)
                .primitive()
                // TODO: pyrite, cobalt, cadmium
                .byproduct(dust("pyrite"), dust("pyrite"), dust("pyrite"))
                .build().buildObject();

        PYRITE = set("pyrite")
                .color(0xFF967828).icon(IconSet.ROUGH)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                .primitive()
                // TODO: sulfur, tricalcium
                .byproduct(dust("pyrite"), dust("pyrite"))
                .build().buildObject();

        CASSITERITE = set("cassiterite")
                .color(0xFFDCDCDC).icon(IconSet.METALLIC)
                .smelt(ingot("tin"))
                .ore(OreVariant.STONE, 2)
                .hammer()
                // TODO: tin, zinc
                .byproduct(dust("tin"), dust("tin"))
                .build().createObject();

        MAGNETITE = set("magnetite")
                .color(0xFF1E1E1E).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .ore(OreVariant.DEEPSLATE)
                .byproduct(dust("magnetite"), dust("gold"))
                .build()
                .buildObject();
    }

    public static void init() {}

    public static void initRecipes() {
        VEINS.vein("chalcopyrite", 0.4)
                .primitive()
                .ore(CHALCOPYRITE, 0.5)
                .ore(PYRITE, 0.5)
                .build()
                .vein("cassiterite", 0.2)
                .ore(CASSITERITE, 0.5)
                .ore(TIN, 0.5)
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
            this.builder = AllRecipes.ORE_ANALYZER.recipe(id).rate(rate);
        }

        public VeinBuilder ore(MaterialSet material, double rate) {
            builder.outputItem(material.entry("raw"), rate);
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

        @Override
        protected Unit createObject() {
            assert variant != null;
            assert rate > 0d;
            if (!primitive) {
                builder.voltage(variant.voltage);
            }
            var tech = AllTechs.ORE.get(variant);
            var baseProgress = 10L * (1L << (long) variant.rank);
            var oreProgress = (long) (2d / rate);
            if (!baseOre) {
                tech = REGISTRATE.tech("ore/" + id)
                        .maxProgress(baseProgress * oreProgress)
                        .depends(tech).buildObject();

                AllRecipes.RESEARCH.recipe(tech)
                        .target(tech)
                        .inputItem(() -> variant.baseItem)
                        .voltage(variant.voltage)
                        .workTicks(200)
                        .build();
            }

            for (var ore : ores) {
                AllRecipes.RESEARCH.recipe(tech.getPath() + "_from_" + ore.name)
                        .target(tech).progress(oreProgress)
                        .inputItem(ore.tag("raw"))
                        .voltage(variant.voltage)
                        .workTicks(200)
                        .build();
            }

            builder.requireTech(tech).build();
            return Unit.INSTANCE;
        }
    }
}
