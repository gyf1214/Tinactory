package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.datagen.builder.TechBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllRecipes.RESEARCH;
import static org.shsts.tinactory.content.AllTechs.BASE_ORE;
import static org.shsts.tinactory.content.AllTechs.LOGISTICS;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Technologies {
    public static void init() {
        var $ = new TechFactory();
        for (var variant : OreVariant.values()) {
            $.tech(BASE_ORE.get(variant))
                    .maxProgress(200L * (1L << (long) variant.rank))
                    .displayItem(variant.baseItem)
                    .build();
        }
        $.reset();
        for (var i = 0; i < LOGISTICS.size(); i++) {
            $.tech(LOGISTICS.get(i))
                    .maxProgress(30L * (1L << (2L * i)))
                    .modifier("logistics_level", 1)
                    .build();
        }

        for (var entry : BASE_ORE.entrySet()) {
            var variant = entry.getKey();
            var tech = entry.getValue();
            RESEARCH.recipe(DATA_GEN, tech)
                    .target(tech)
                    .defaultInput(variant.voltage)
                    .build();
        }

        RESEARCH.recipe(DATA_GEN, LOGISTICS.get(0))
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
