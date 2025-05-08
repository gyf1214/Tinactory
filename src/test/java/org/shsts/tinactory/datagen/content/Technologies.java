package org.shsts.tinactory.datagen.content;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.AllMultiblocks;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.datagen.builder.TechBuilder;
import org.shsts.tinactory.datagen.provider.TechProvider;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.builder.VeinBuilder.VEIN_TECH_RANK;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Technologies {
    public static IDataHandler<TechProvider> TECHS;

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
    public static ResourceLocation KANTHAL;
    public static ResourceLocation SIFTING;
    public static ResourceLocation INTEGRATED_CIRCUIT;
    public static ResourceLocation COLD_WORKING;
    public static ResourceLocation ELECTROLYZING;
    public static ResourceLocation VACUUM_FREEZER;
    public static ResourceLocation CHEMISTRY;
    public static ResourceLocation OIL_PROCESSING;
    public static ResourceLocation ORGANIC_CHEMISTRY;
    public static ResourceLocation AUTOFARM;
    public static ResourceLocation CPU;
    public static ResourceLocation DISTILLATION;
    public static ResourceLocation CLEANROOM;

    public static void init() {
        TECHS = DATA_GEN.createHandler(TechProvider::new);

        baseOreTech();

        var factory = new TechFactory();

        ALLOY_SMELTING = factory.child("alloy_smelting")
            .maxProgress(20L)
            .displayItem(ALLOY_SMELTER.entry(Voltage.ULV))
            .register();

        STEEL = factory.child("steel")
            .maxProgress(30L)
            .displayItem(AllMaterials.STEEL.entry("ingot"))
            .register();

        ELECTRIC_HEATING = factory.tech("electric_heating")
            .maxProgress(30L)
            .displayItem(AllMaterials.COPPER.item("wire"))
            .register();

        BATTERY = factory.tech("battery")
            .maxProgress(40L)
            .displayItem(AllItems.BATTERY.get(Voltage.LV))
            .register();

        MOTOR = factory.child("motor")
            .maxProgress(30L)
            .displayItem(AllItems.ELECTRIC_MOTOR.get(Voltage.LV))
            .register();

        MATERIAL_CUTTING = factory.tech("material_cutting")
            .maxProgress(35L)
            .displayItem(AllItems.BASIC_BUZZSAW)
            .register();

        SENSOR_AND_EMITTER = factory.tech("sensor_and_emitter")
            .maxProgress(40L)
            .displayItem(AllItems.EMITTER.get(Voltage.LV))
            .register();

        PUMP_AND_PISTON = factory.child("pump_and_piston")
            .maxProgress(35L)
            .displayItem(AllItems.ELECTRIC_PISTON.get(Voltage.LV))
            .register();

        HOT_WORKING = factory.child("hot_working")
            .maxProgress(40L)
            .displayItem(Items.BLAZE_POWDER)
            .register();

        CONVEYOR_MODULE = factory.child("conveyor_module")
            .maxProgress(40L)
            .displayItem(AllItems.CONVEYOR_MODULE.get(Voltage.LV))
            .register();

        ROBOT_ARM = factory.child("robot_arm")
            .maxProgress(50L)
            .displayItem(AllItems.ROBOT_ARM.get(Voltage.LV))
            .register();

        factory.voltage(Voltage.LV);

        KANTHAL = factory.tech("kanthal")
            .maxProgress(10L)
            .displayItem(AllMultiblocks.KANTHAL_COIL_BLOCK)
            .register();

        SIFTING = factory.tech("sifting")
            .maxProgress(10L)
            .displayItem(AllItems.ITEM_FILTER)
            .register();

        AUTOFARM = factory.tech("autofarm")
            .maxProgress(15L)
            .displayItem(Items.WHEAT)
            .register();

        INTEGRATED_CIRCUIT = factory.tech("integrated_circuit")
            .maxProgress(20L)
            .displayItem(AllItems.GOOD_INTEGRATED.item())
            .depends(SENSOR_AND_EMITTER, MATERIAL_CUTTING)
            .register();

        COLD_WORKING = factory.tech("cold_working")
            .maxProgress(40L)
            .displayTexture(gregtech("items/metaitems/shape.extruder.rotor"))
            .register();

        ELECTROLYZING = factory.tech("electrolyzing")
            .maxProgress(40L)
            .displayItem(AllMaterials.GOLD.entry("wire"))
            .register();

        VACUUM_FREEZER = factory.tech("vacuum_freezer")
            .maxProgress(50L)
            .displayItem(AllMultiblocks.VACUUM_FREEZER)
            .register();

        DISTILLATION = factory.tech("distillation")
            .maxProgress(50L)
            .displayItem(AllMultiblocks.DISTILLATION_TOWER)
            .register();

        CHEMISTRY = factory.child("chemistry")
            .maxProgress(40L)
            .displayItem(AllItems.RESEARCH_EQUIPMENT.get(Voltage.LV))
            .register();

        OIL_PROCESSING = factory.tech("oil_processing")
            .maxProgress(60L)
            .displayItem(AllMaterials.SULFUR.entry("dust"))
            .register();

        ORGANIC_CHEMISTRY = factory.child("organic_chemistry")
            .maxProgress(50L)
            .displayItem(AllMaterials.PE.entry("sheet"))
            .register();

        CPU = factory.tech("cpu")
            .maxProgress(100L)
            .displayItem(AllItems.WAFERS.get("cpu"))
            .depends(INTEGRATED_CIRCUIT)
            .register();

        factory.base(CHEMISTRY).voltage(Voltage.MV);

        CLEANROOM = factory.tech("cleanroom")
            .maxProgress(40L)
            .displayItem(AllMultiblocks.CLEANROOM)
            .register();
    }

    private static void baseOreTech() {
        BASE_ORE = new HashMap<>();
        ResourceLocation prev = null;
        for (var variant : OreVariant.values()) {
            var prev1 = prev;
            var tech = TECHS.builder("ore_base/" + variant.name().toLowerCase(), TechBuilder::factory)
                .maxProgress(20L)
                .displayItem(variant.baseItem)
                .researchVoltage(variant.voltage)
                .rank(VEIN_TECH_RANK)
                .transform($ -> prev1 != null ? $.depends(prev1) : $)
                .register();
            BASE_ORE.put(variant, tech);
            prev = tech;
        }
    }

    private static class TechFactory {
        @Nullable
        private ResourceLocation base = null;
        private Voltage baseVoltage = Voltage.ULV;
        private int rank = 0;

        public TechBuilder<TechFactory> child(String id) {
            var builder = tech(id);
            base = modLoc(id);
            return builder;
        }

        public TechBuilder<TechFactory> tech(String id) {
            var builder = TECHS.builder(this, id, TechBuilder::factory)
                .rank(rank++);
            if (base != null) {
                builder.depends(base);
            }
            builder.researchVoltage(baseVoltage);
            return builder;
        }

        public TechFactory voltage(Voltage val) {
            baseVoltage = val;
            return this;
        }

        public TechFactory base(ResourceLocation val) {
            base = val;
            return this;
        }

        public TechFactory reset() {
            base = null;
            return this;
        }
    }
}
