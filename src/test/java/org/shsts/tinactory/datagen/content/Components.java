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
import static org.shsts.tinactory.content.AllItems.DUMMY_ITEMS;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.HEAT_PROOF_BLOCK;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
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
        componentRecipes();
        misc();
    }

    private static void componentItems() {
        DATA_GEN.item(VACUUM_TUBE)
                .model(basicItem("metaitems/circuit.vacuum_tube"))
                .build();

        DUMMY_ITEMS.forEach(entry -> DATA_GEN.item(entry)
                .model(Models::componentItem)
                .build());

        BATTERY.forEach((v, entry) -> DATA_GEN.item(entry)
                .model(basicItem("metaitems/battery.re." + v.id + ".lithium"))
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
                        .unlockedBy("has_wire", has(IRON.tag("wire"))))
                .vanillaRecipe(() -> ShapedRecipeBuilder
                        .shaped(VACUUM_TUBE.get())
                        .pattern("BGB").pattern("WWW")
                        .define('G', Items.GLASS)
                        .define('W', COPPER.tag("wire"))
                        .define('B', IRON.tag("bolt"))
                        .unlockedBy("has_wire", has(COPPER.tag("wire"))));

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
        componentRecipe(Voltage.LV, STEEL, COPPER, BRONZE, TIN, STEEL);
        componentRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, STEEL, BRONZE, STEEL);
    }

    private static void misc() {
        DATA_GEN.block(HEAT_PROOF_BLOCK)
                .blockState(solidBlock("casings/solid/machine_casing_heatproof"))
                .tag(MINEABLE_WITH_WRENCH)
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
                .build()
                .recipe(DATA_GEN, ELECTRIC_PUMP.get(voltage))
                .outputItem(2, ELECTRIC_PUMP.get(voltage), 1)
                .inputItem(0, motor, 1)
                .inputItem(0, pipeMaterial.tag("pipe"), 1)
                .inputItem(0, rotorMaterial.tag("rotor"), 1)
                .inputItem(0, rotorMaterial.tag("screw"), 3)
                // TODO rubber seal
                .inputItem(0, cable, 1)
                .workTicks(ticks)
                .voltage(v)
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
                .build()
                .recipe(DATA_GEN, MACHINE_HULL.get(voltage))
                .outputItem(2, MACHINE_HULL.get(voltage), 1)
                .inputItem(0, mainMaterial.tag("plate"), 8)
                .inputItem(0, cable, 2)
                .workTicks(ticks)
                .voltage(v)
                .build();
    }
}
