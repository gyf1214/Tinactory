package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTechs;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.dust;
import static org.shsts.tinactory.content.AllMaterials.ingot;
import static org.shsts.tinactory.content.AllMaterials.primary;
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
                // TODO: pyrite, cobalt, cadmium?
                .byproduct("wash", dust("pyrite"))
                .build().buildObject();

        PYRITE = set("pyrite")
                .color(0xFF967828).icon(IconSet.ROUGH)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                .primitive()
                // TODO: sulfur, tricalcium phosphate?
                .build().buildObject();

        LIMONITE = set("limonite")
                .color(0xFFC8C800).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                // TODO: nickel, cobalt
                .byproduct("wash", dust("nickel"))
                .build().buildObject();

        BANDED_IRON = set("banded_iron")
                .color(0xFF915A5A).icon(IconSet.DULL)
                .smelt(ingot("iron"))
                .ore(OreVariant.STONE)
                // TODO: magnetite, magnesium?
                .byproduct("wash", dust("magnetite"))
                .build().buildObject();

        COAL = set("coal")
                .color(0xFF464646).icon(IconSet.DULL)
                .existing("primary", Items.COAL)
                .ore(OreVariant.STONE, 2)
                // TODO: coal, thorium
                .byproduct(primary("coal"))
                .build().buildObject();

        CASSITERITE = set("cassiterite")
                .color(0xFFDCDCDC).icon(IconSet.METALLIC)
                .smelt(ingot("tin"))
                .ore(OreVariant.STONE, 2)
                // TODO: tin, zinc
                .byproduct(dust("tin"))
                .build().buildObject();

        REDSTONE = set("redstone")
                .color(0xFFC80000).icon(IconSet.DULL)
                .existing("dust", Tags.Items.DUSTS_REDSTONE, Items.REDSTONE)
                .ore(OreVariant.STONE, 5)
                // TODO: cinnabar, glowstone, rare earth?
                .build().buildObject();

        CINNABAR = set("cinnabar")
                .color(0xFF960000).icon(IconSet.SHINY)
                .ore(OreVariant.STONE)
                // TODO: glowstone, rare earth?
                .byproduct(dust("redstone"))
                .build().buildObject();

        RUBY = set("ruby")
                .color(0xFFFF6464).icon(IconSet.RUBY)
                .ore(OreVariant.STONE)
                // TODO: cinnabar, chromium
                .byproduct(dust("cinnabar"))
                .build().buildObject();

        MAGNETITE = set("magnetite")
                .color(0xFF1E1E1E).icon(IconSet.METALLIC)
                .smelt(ingot("iron"))
                .ore(OreVariant.DEEPSLATE)
                .byproduct(dust("gold"))
                .byproduct("wash", dust("iron"))
                .build()
                .buildObject();
    }

    public static void init() {}

    public static void initRecipes() {
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
            this.builder = AllRecipes.ORE_ANALYZER.recipe(id).rate(rate);
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
            var tech = AllTechs.ORE.get(variant);
            var baseProgress = 50L * (1L << (long) variant.rank);
            if (!baseOre) {
                tech = REGISTRATE.tech("ore/" + id)
                        .maxProgress(baseProgress)
                        .displayItem(ores.get(0).loc("raw"))
                        .depends(tech).buildObject();

                AllRecipes.RESEARCH.recipe(tech)
                        .target(tech)
                        .defaultInput(variant.voltage)
                        .build();
            }

            for (var ore : new HashSet<>(ores)) {
                AllRecipes.RESEARCH.recipe(tech.getPath() + "_from_" + ore.name)
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
