package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.datagen.builder.TechBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Technologies {
    private static final TechFactory TECH_FACTORY;
    public static final Map<OreVariant, ResourceLocation> BASE_ORE;
    public static final ResourceLocation ALLOY_SMELTING;

    static {
        TECH_FACTORY = new TechFactory();

        BASE_ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            var tech = TECH_FACTORY.child("ore/" + variant.name().toLowerCase())
                    .maxProgress(200L * (1L << (long) variant.rank))
                    .displayItem(variant.baseItem)
                    .researchVoltage(variant.voltage)
                    .buildLoc();
            BASE_ORE.put(variant, tech);
        }
        TECH_FACTORY.reset();

        ALLOY_SMELTING = TECH_FACTORY.reset()
                .tech("alloy_smelting")
                .maxProgress(20L)
                .displayItem(ALLOY_SMELTER.entry(Voltage.ULV))
                .researchVoltage(Voltage.ULV)
                .buildLoc();
    }

    public static void init() {}

    private static class TechFactory {
        @Nullable
        private ResourceLocation base = null;

        public TechBuilder<TechFactory> child(String id) {
            var builder = DATA_GEN.tech(this, id);
            if (base != null) {
                builder.depends(base);
            }
            base = modLoc(id);
            return builder;
        }

        public TechBuilder<TechFactory> tech(String id) {
            var builder = DATA_GEN.tech(this, id);
            if (base != null) {
                builder.depends(base);
            }
            return builder;
        }

        public TechFactory reset() {
            base = null;
            return this;
        }
    }
}
