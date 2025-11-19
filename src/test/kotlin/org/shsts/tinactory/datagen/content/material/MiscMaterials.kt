package org.shsts.tinactory.datagen.content.material

import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.FLUID_STORAGE_CELL
import org.shsts.tinactory.content.AllTags.ITEM_STORAGE_CELL
import org.shsts.tinactory.content.AllTags.STORAGE_CELL
import org.shsts.tinactory.content.AllTags.TOOL
import org.shsts.tinactory.content.AllTags.TOOL_FILE
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_MORTAR
import org.shsts.tinactory.content.AllTags.TOOL_SAW
import org.shsts.tinactory.content.AllTags.TOOL_SCREW
import org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER
import org.shsts.tinactory.content.AllTags.TOOL_SHEARS
import org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.cubeTint
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.DataFactories.itemData
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.stoneGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla

object MiscMaterials {
    fun init() {
        itemData("rubber_tree/sticky_resin") {
            model(basicItem("metaitems/rubber_drop"))
        }

        blockData("material/block/coke") {
            blockState(cubeTint("material_sets/lignite/block"))
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
        }

        // disable vanilla recipes
        disableVanilla("iron")
        disableVanilla("gold")
        disableVanilla("copper")
        disableVanilla("coal", "")
        disableVanilla("diamond", "")
        disableVanilla("redstone", "")
        disableVanilla("lapis", "lazuli")
        disableVanilla("emerald", "")
        vanilla {
            nullRecipe("quartz", "quartz_from_blasting", "quartz_block")
        }

        // smelt iron nugget to wrought iron
        vanilla {
            smelting(getMaterial("iron").tag("nugget"),
                getMaterial("wrought_iron").item("nugget"),
                200, suffix = "_from_iron")
        }

        // MV freezer
        vacuumFreezer {
            defaults {
                voltage(Voltage.MV)
            }
            output("air", "liquid") {
                input("air")
                workTicks(200)
            }
            output("water") {
                input("water", "gas")
                workTicks(32)
            }
            output(Items.ICE) {
                input("water")
                workTicks(64)
            }
            output(Items.PACKED_ICE) {
                input(Items.ICE, 5)
                workTicks(160)
            }
        }

        // HV freezer
        vacuumFreezer {
            defaults {
                voltage(Voltage.HV)
            }
            output(Items.BLUE_ICE) {
                input(Items.PACKED_ICE, 5)
                workTicks(160)
            }
            output("coolant") {
                input("coolant", "gas")
                workTicks(32)
            }
            output("oxygen", "liquid") {
                input("oxygen")
                workTicks(200)
            }
        }

        mixer {
            output("coolant", amount = 10) {
                input(Items.BLUE_ICE)
                input("sodium")
                input("potassium")
                voltage(Voltage.HV)
                workTicks(200)
            }
        }

        generateStone()
        stone()
        tags()
    }

    private val VANILLA_METHODS = listOf("smelting", "blasting")

    private fun disableVanilla(name: String, suffix: String = "ingot") {
        val fullName = if (suffix.isEmpty()) name else "${name}_$suffix"

        vanilla {
            nullRecipe("${name}_block")

            if (suffix == "ingot") {
                nullRecipe("raw_$name")
                nullRecipe("raw_${name}_block")
                if (name == "copper") {
                    nullRecipe(fullName)
                    nullRecipe("${fullName}_from_waxed_copper_block")
                } else {
                    nullRecipe("${fullName}_from_${name}_block")
                    nullRecipe("${fullName}_from_nuggets")
                    nullRecipe("${name}_nugget")
                    for (method in VANILLA_METHODS) {
                        nullRecipe("${name}_nugget_from_$method")
                    }
                }
            } else {
                nullRecipe(fullName)
            }

            val ores = mutableListOf("", "_deepslate").apply {
                if (name == "gold") {
                    add("_nether")
                }
            }

            for (method in VANILLA_METHODS) {
                for (ore in ores) {
                    nullRecipe("${fullName}_from_${method}${ore}_${name}_ore")
                }
                if (suffix == "ingot") {
                    nullRecipe("${fullName}_from_${method}_raw_$name")
                }
            }
        }
    }

    private fun generateStone() {
        stoneGenerator {
            output(Items.COBBLESTONE) {
                voltage(Voltage.PRIMITIVE)
            }
            output("water") {
                voltage(Voltage.ULV)
            }
            output(Items.COBBLED_DEEPSLATE) {
                voltage(Voltage.LV)
            }
            output("air") {
                voltage(Voltage.MV)
            }
            output("sea_water") {
                voltage(Voltage.MV)
            }
            output(Items.NETHERRACK) {
                voltage(Voltage.HV)
                tech(Technologies.ROCKET_T1)
            }
            output(Items.END_STONE) {
                voltage(Voltage.EV)
                tech(Technologies.ROCKET_T2)
            }
        }
    }

    private fun stone() {
        toolCrafting {
            // stone -> gravel
            result(Items.GRAVEL) {
                pattern("#")
                pattern("#")
                define('#', "stone", "primary")
                toolTag(TOOL_HAMMER)
            }
            // gravel -> flint
            result("flint", "primary") {
                pattern("###")
                define('#', Items.GRAVEL)
                toolTag(TOOL_HAMMER)
            }
            // gravel -> sand
            shapeless(Items.GRAVEL, Items.SAND, TOOL_MORTAR)
        }

        // macerate stones
        macerator {
            defaults {
                voltage(Voltage.LV)
            }
            input("glass", "primary") {
                output("silicon_dioxide", "dust")
                workTicks(128)
            }
            input("flint", "primary") {
                output("silicon_dioxide", "dust")
                workTicks(128)
            }
            input(Items.GRAVEL) {
                output(Items.SAND)
                workTicks(64)
            }
            input(Items.SANDSTONE) {
                output(Items.SAND, 4)
                workTicks(240)
            }
        }

        // centrifuge stones
        centrifuge {
            defaults {
                voltage(Voltage.LV)
            }
            input(Items.SAND) {
                output("silicon_dioxide", "dust")
                workTicks(64)
            }
            input("stone", "dust", 2) {
                output("silicon_dioxide", "dust")
                output("calcium_carbonate", "dust")
                workTicks(128)
            }
            input("stone", "primary", 2) {
                output(Items.GRAVEL)
                output("calcium_carbonate", "dust")
                workTicks(240)
            }
        }

        // sift gravel
        sifter {
            input(Items.GRAVEL) {
                output("flint", "primary", rate = 0.8)
                output("flint", "primary", rate = 0.35)
                output(Items.SAND, rate = 0.65)
                voltage(Voltage.LV)
                workTicks(64)
            }
        }
    }

    private fun tags() {
        dataGen {
            tag(TOOL_HAMMER, TOOL)
            tag(TOOL_MORTAR, TOOL)
            tag(TOOL_FILE, TOOL)
            tag(TOOL_SAW, TOOL)
            tag(TOOL_SCREWDRIVER, TOOL)
            tag(TOOL_WRENCH, TOOL)
            tag(TOOL_WIRE_CUTTER, TOOL)
            tag({ Items.SHEARS }, TOOL_SHEARS)
            tag({ Items.STICK }, TOOL_HANDLE)
            tag(getMaterial("wrought_iron").tag("stick"), TOOL_HANDLE)
            tag(getMaterial("iron").tag("screw"), TOOL_SCREW)
            tag(ITEM_STORAGE_CELL, STORAGE_CELL)
            tag(FLUID_STORAGE_CELL, STORAGE_CELL)
        }
    }
}
