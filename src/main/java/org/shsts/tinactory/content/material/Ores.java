package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SimpleBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
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
                .ore(OreVariant.STONE)
                .amount(2)
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
        VEINS.vein("chalcopyrite")
                .primitive()
                .ore(CHALCOPYRITE, 0.5f)
                .ore(PYRITE, 0.5f)
                .build()
                .vein("cassiterite")
                .ore(CASSITERITE, 0.5f)
                .ore(TIN, 0.5f)
                .build()
                .vein("magnetite")
                .ore(MAGNETITE, 0.5f)
                .ore(GOLD, 0.1f)
                .build();
    }

    private static class VeinFactory {
        public VeinBuilder vein(String id) {
            return new VeinBuilder(this, id);
        }
    }

    private static final VeinFactory VEINS = new VeinFactory();

    private static class VeinBuilder extends SimpleBuilder<Unit, VeinFactory, VeinBuilder> {
        private record OreInfo(MaterialSet material, float rate) {}

        private final String id;
        private final List<OreInfo> ores = new ArrayList<>();
        private boolean primitive = false;

        public VeinBuilder(VeinFactory parent, String id) {
            super(parent);
            this.id = id;
        }

        public VeinBuilder ore(MaterialSet material, float rate) {
            ores.add(new OreInfo(material, rate));
            return this;
        }

        private VeinBuilder primitive() {
            primitive = true;
            return this;
        }

        @Override
        protected Unit createObject() {
            assert !ores.isEmpty();
            var variant = ores.get(0).material.oreVariant();
            assert ores.stream().allMatch(x -> x.material.oreVariant() == variant);
            var builder = AllRecipes.ORE_ANALYZER.recipe(id)
                    .inputItem(0, () -> variant.baseItem, 1);
            for (var ore : ores) {
                builder.outputItem(1, ore.material.entry("raw"), 1, ore.rate);
            }
            builder.voltage(primitive ? Voltage.PRIMITIVE : variant.voltage)
                    .build();

            return Unit.INSTANCE;
        }
    }
}
