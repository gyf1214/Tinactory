package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.AllItems;
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
    public static Map<OreVariant, ResourceLocation> BASE_ORE;
    public static ResourceLocation ALLOY_SMELTING;
    public static ResourceLocation STEEL;
    public static ResourceLocation MOTOR;
    public static ResourceLocation PUMP_AND_PISTON;
    public static ResourceLocation ELECTRIC_HEATING;
    public static ResourceLocation MATERIAL_CUTTING;
    public static ResourceLocation CONVEYOR_MODULE;
    public static ResourceLocation BATTERY;
    public static ResourceLocation SENSOR_AND_EMITTER;
    public static ResourceLocation HOT_WORKING;
    public static ResourceLocation ROBOT_ARM;
    public static ResourceLocation INTEGRATED_CIRCUIT;

    public static void init() {
        var factory = new TechFactory();

        BASE_ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            var tech = factory.child("ore_base/" + variant.name().toLowerCase())
                    .maxProgress(20L * (1L << (long) variant.rank))
                    .displayItem(variant.baseItem)
                    .researchVoltage(variant.voltage)
                    .buildLoc();
            BASE_ORE.put(variant, tech);
        }

        factory.reset().voltage(Voltage.ULV);

        ALLOY_SMELTING = factory.child("alloy_smelting")
                .maxProgress(20L)
                .displayItem(ALLOY_SMELTER.entry(Voltage.ULV))
                .buildLoc();

        STEEL = factory.child("steel")
                .maxProgress(30L)
                .displayItem(AllMaterials.STEEL.entry("ingot"))
                .buildLoc();

        ELECTRIC_HEATING = factory.tech("electric_heating")
                .maxProgress(30L)
                .displayItem(AllMaterials.COPPER.item("wire"))
                .buildLoc();

        MOTOR = factory.child("motor")
                .maxProgress(30L)
                .displayItem(AllItems.ELECTRIC_MOTOR.get(Voltage.LV))
                .buildLoc();

        PUMP_AND_PISTON = factory.tech("pump_and_piston")
                .maxProgress(35L)
                .displayItem(AllItems.ELECTRIC_PISTON.get(Voltage.LV))
                .buildLoc();

        MATERIAL_CUTTING = factory.tech("material_cutting")
                .maxProgress(35L)
                .displayItem(AllItems.BASIC_BUZZSAW)
                .buildLoc();

        CONVEYOR_MODULE = factory.tech("conveyor_module")
                .maxProgress(40L)
                .displayItem(AllItems.CONVEYOR_MODULE.get(Voltage.LV))
                .buildLoc();

        BATTERY = factory.tech("battery")
                .maxProgress(40L)
                .displayItem(AllItems.BATTERY.get(Voltage.LV))
                .buildLoc();

        SENSOR_AND_EMITTER = factory.tech("sensor_and_emitter")
                .maxProgress(40L)
                .displayItem(AllItems.EMITTER.get(Voltage.LV))
                .buildLoc();

        HOT_WORKING = factory.base(PUMP_AND_PISTON).tech("hot_working")
                .maxProgress(40L)
                .displayItem(Items.BLAZE_POWDER)
                .buildLoc();

        ROBOT_ARM = factory.child("robot_arm")
                .maxProgress(50L)
                .displayItem(AllItems.ROBOT_ARM.get(Voltage.LV))
                .buildLoc();

        factory.voltage(Voltage.LV);

        INTEGRATED_CIRCUIT = factory.tech("integrated_circuit")
                .maxProgress(20L)
                .displayItem(AllItems.GOOD_INTEGRATED.item())
                .depends(CONVEYOR_MODULE, SENSOR_AND_EMITTER, MATERIAL_CUTTING)
                .buildLoc();
    }

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

        public TechFactory base(ResourceLocation loc) {
            base = loc;
            return this;
        }
    }
}
