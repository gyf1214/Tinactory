package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.AllMaterials;
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
    public static final ResourceLocation STEEL;

    static {
        TECH_FACTORY = new TechFactory();

        BASE_ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            var tech = TECH_FACTORY.child("ore/" + variant.name().toLowerCase())
                    .maxProgress(20L * (1L << (long) variant.rank))
                    .displayItem(variant.baseItem)
                    .researchVoltage(variant.voltage)
                    .buildLoc();
            BASE_ORE.put(variant, tech);
        }

        TECH_FACTORY.reset().voltage(Voltage.ULV);

        ALLOY_SMELTING = TECH_FACTORY.child("alloy_smelting")
                .maxProgress(20L)
                .displayItem(ALLOY_SMELTER.entry(Voltage.ULV))
                .researchVoltage(Voltage.ULV)
                .buildLoc();

        STEEL = TECH_FACTORY.tech("steel")
                .maxProgress(30L)
                .displayItem(AllMaterials.STEEL.entry("ingot"))
                .researchVoltage(Voltage.ULV)
                .buildLoc();
    }

    public static void init() {}

    private static class TechFactory {
        @Nullable
        private ResourceLocation base = null;
        @Nullable
        private Voltage baseVoltage = null;

        public TechBuilder<TechFactory> child(String id) {
            var builder = tech(id);
            base = modLoc(id);
            return builder;
        }

        public TechBuilder<TechFactory> tech(String id) {
            var builder = DATA_GEN.tech(this, id);
            if (base != null) {
                builder.depends(base);
            }
            if (baseVoltage != null) {
                builder.researchVoltage(baseVoltage);
            }
            return builder;
        }

        public TechFactory voltage(Voltage val) {
            baseVoltage = val;
            return this;
        }

        public TechFactory reset() {
            base = null;
            return this;
        }
    }
}
