package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.datagen.builder.TechBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE;
import static org.shsts.tinactory.content.AllRecipes.RESEARCH_BENCH;
import static org.shsts.tinactory.content.AllTechs.BASE_ORE;
import static org.shsts.tinactory.content.AllTechs.LOGISTICS;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Technologies {
    private static final TechFactory TECH_FACTORY = new TechFactory();

    public static void init() {
        for (var variant : OreVariant.values()) {
            TECH_FACTORY.tech(BASE_ORE.get(variant))
                    .maxProgress(200L * (1L << (long) variant.rank))
                    .displayItem(variant.baseItem)
                    .build();
        }
        TECH_FACTORY.reset();
        for (var i = 0; i < LOGISTICS.size(); i++) {
            TECH_FACTORY.tech(LOGISTICS.get(i))
                    .maxProgress(30L * (1L << (2L * i)))
                    .modifier("logistics_level", 1)
                    .displayItem(CONVEYOR_MODULE.get(Voltage.fromRank(2 + 2 * i)))
                    .build();
        }

        for (var entry : BASE_ORE.entrySet()) {
            var variant = entry.getKey();
            var tech = entry.getValue();
            RESEARCH_BENCH.recipe(DATA_GEN, tech)
                    .target(tech)
                    .defaultInput(variant.voltage)
                    .build();
        }

        RESEARCH_BENCH.recipe(DATA_GEN, LOGISTICS.get(0))
                .target(LOGISTICS.get(0))
                .defaultInput(Voltage.LV)
                .build();
    }

    private static class TechFactory {
        @Nullable
        private ResourceLocation base = null;

        public TechBuilder<TechFactory> tech(ResourceLocation loc) {
            var builder = DATA_GEN.tech(this, loc);
            if (base != null) {
                builder.depends(base);
            }
            base = loc;
            return builder;
        }

        public TechFactory reset() {
            base = null;
            return this;
        }
    }
}
