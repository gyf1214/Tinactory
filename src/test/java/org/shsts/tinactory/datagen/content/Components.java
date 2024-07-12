package org.shsts.tinactory.datagen.content;


import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllItems.BATTERY;
import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.CUPRONICKEL_COIL_BLOCK;
import static org.shsts.tinactory.content.AllItems.DUMMY_ITEMS;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT;
import static org.shsts.tinactory.content.AllItems.HEAT_PROOF_BLOCK;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.machineItem;
import static org.shsts.tinactory.datagen.content.Models.solidBlock;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    private static final String RESEARCH_TEX = "metaitems/glass_vial/";
    private static final int ASSEMBLE_TICKS = 100;

    public static void init() {
        componentItems();
        ulv();
        misc();
        componentRecipes();
        circuitRecipes();
        miscRecipes();
    }

    private static void componentItems() {
        DATA_GEN.item(VACUUM_TUBE)
                .model(basicItem("metaitems/circuit.vacuum_tube"))
                .tag(AllTags.circuit(Voltage.ULV))
                .build();

        DATA_GEN.item(ELECTRONIC_CIRCUIT)
                .model(basicItem("metaitems/circuit.electronic"))
                .tag(AllTags.circuit(Voltage.LV))
                .build();

        DUMMY_ITEMS.forEach(entry -> DATA_GEN.item(entry)
                .model(Models::componentItem)
                .build());

        BATTERY.forEach((v, entry) -> DATA_GEN.item(entry)
                .model(Models::batteryItem)
                .build());

        MACHINE_HULL.forEach((v, entry) -> DATA_GEN.item(entry)
                .model(machineItem(v, IO_TEX))
                .build());

        RESEARCH_EQUIPMENT.forEach((v, entry) -> DATA_GEN.item(entry)
                .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
                .build());

        CABLE.forEach((v, entry) -> DATA_GEN.block(entry)
                .blockState(Models::cableBlock)
                .itemModel(Models::cableItem)
                .tag(MINEABLE_WITH_CUTTER)
                .build());
    }

    private static void ulv() {
        DATA_GEN.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(CABLE.get(Voltage.ULV).get())
                .requires(Ingredient.of(IRON.tag("wire")), 4)
                .unlockedBy("has_wire", has(IRON.tag("wire"))));

        TOOL_CRAFTING.recipe(DATA_GEN, MACHINE_HULL.get(Voltage.ULV))
                .result(MACHINE_HULL.get(Voltage.ULV), 1)
                .pattern("###").pattern("#W#").pattern("###")
                .define('#', IRON.tag("plate"))
                .define('W', CABLE.get(Voltage.ULV))
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        ASSEMBLER.recipe(DATA_GEN, RESEARCH_EQUIPMENT.get(Voltage.ULV))
                .outputItem(2, RESEARCH_EQUIPMENT.get(Voltage.ULV), 1)
                .inputItem(0, IRON.tag("plate"), 1)
                .inputItem(0, COPPER.tag("wire"), 1)
                .workTicks(200)
                .voltage(Voltage.ULV)
                .build();
    }

    private static void componentRecipes() {
        TOOL_CRAFTING.recipe(DATA_GEN, CABLE.get(Voltage.LV))
                .result(CABLE.get(Voltage.LV), 1)
                .pattern("WWR").pattern("WWR").pattern("RR ")
                .define('W', TIN.tag("wire"))
                .define('R', RUBBER.tag("plate"))
                .toolTag(TOOL_WIRE_CUTTER)
                .build();

        componentRecipe(Voltage.LV, STEEL, COPPER, BRONZE, TIN, STEEL);
        componentRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, STEEL, BRONZE, STEEL);
    }

    private static void misc() {
        DATA_GEN.block(HEAT_PROOF_BLOCK)
                .blockState(solidBlock("casings/solid/machine_casing_heatproof"))
                .tag(MINEABLE_WITH_WRENCH)
                .build()
                .block(CUPRONICKEL_COIL_BLOCK)
                .blockState(solidBlock("casings/coils/machine_coil_cupronickel"))
                .build()
                .item(STICKY_RESIN)
                .model(basicItem("metaitems/rubber_drop"))
                .build();
    }

    private static void componentRecipe(Voltage voltage, MaterialSet mainMaterial,
                                        MaterialSet heatMaterial, MaterialSet pipeMaterial,
                                        MaterialSet rotorMaterial, MaterialSet magneticMaterial) {

        var v = voltage == Voltage.LV ? Voltage.ULV : Voltage.LV;
        var ticks = ASSEMBLE_TICKS;

        var cable = CABLE.get(voltage);
        var motor = ELECTRIC_MOTOR.get(voltage);

        ASSEMBLER.recipe(DATA_GEN, motor)
                .outputItem(2, motor, 1)
                .inputItem(0, magneticMaterial.tag("magnetic"), 1)
                .inputItem(0, mainMaterial.tag("stick"), 2)
                .inputItem(0, heatMaterial.tag("wire"), 2 * v.rank)
                .inputItem(0, cable, 2)
                .workTicks(ticks)
                .voltage(v)
                .requireTech(Technologies.MOTOR)
                .build()
                .recipe(DATA_GEN, ELECTRIC_PUMP.get(voltage))
                .outputItem(2, ELECTRIC_PUMP.get(voltage), 1)
                .inputItem(0, motor, 1)
                .inputItem(0, pipeMaterial.tag("pipe"), 1)
                .inputItem(0, rotorMaterial.tag("rotor"), 1)
                .inputItem(0, rotorMaterial.tag("screw"), 3)
                .inputItem(0, RUBBER.tag("ring"), 2)
                .inputItem(0, cable, 1)
                .workTicks(ticks)
                .voltage(v)
                .requireTech(Technologies.PUMP_AND_PISTON)
                .build()
                .recipe(DATA_GEN, ELECTRIC_PISTON.get(voltage))
                .outputItem(2, ELECTRIC_PISTON.get(voltage), 1)
                .inputItem(0, motor, 1)
                .inputItem(0, mainMaterial.tag("plate"), 3)
                .inputItem(0, mainMaterial.tag("stick"), 2)
                .inputItem(0, mainMaterial.tag("gear"), 1)
                .inputItem(0, cable, 2)
                .workTicks(ticks)
                .voltage(v)
                .requireTech(Technologies.PUMP_AND_PISTON)
                .build()
                .recipe(DATA_GEN, MACHINE_HULL.get(voltage))
                .outputItem(2, MACHINE_HULL.get(voltage), 1)
                .inputItem(0, mainMaterial.tag("plate"), 8)
                .inputItem(0, cable, 2)
                .workTicks(ticks)
                .voltage(v)
                .build();
    }

    private static void circuitRecipes() {
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(VACUUM_TUBE.get())
                .pattern("BGB").pattern("WWW")
                .define('G', Items.GLASS)
                .define('W', COPPER.tag("wire"))
                .define('B', IRON.tag("bolt"))
                .unlockedBy("has_wire", has(COPPER.tag("wire"))));
    }

    private static void miscRecipes() {
        ASSEMBLER.recipe(DATA_GEN, HEAT_PROOF_BLOCK)
                .outputItem(2, HEAT_PROOF_BLOCK, 1)
                .inputItem(0, INVAR.entry("plate"), 3)
                .inputItem(0, INVAR.entry("stick"), 2)
                .workTicks(82L)
                .voltage(Voltage.ULV)
                .requireTech(Technologies.STEEL)
                .build()
                .recipe(DATA_GEN, CUPRONICKEL_COIL_BLOCK)
                .outputItem(2, CUPRONICKEL_COIL_BLOCK, 1)
                .inputItem(0, CUPRONICKEL.entry("wire"), 8)
                .inputItem(0, BRONZE.entry("foil"), 8)
                .workTicks(200L)
                .voltage(Voltage.ULV)
                .requireTech(Technologies.STEEL)
                .build();
    }
}
