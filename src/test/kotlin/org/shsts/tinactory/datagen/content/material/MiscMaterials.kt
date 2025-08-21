package org.shsts.tinactory.datagen.content.material

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
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.stoneGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolShapeless
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

object MiscMaterials {
    fun init() {
        // blast ores
        blast()

        // disable vanilla recipes
        disableVanilla("iron")
        disableVanilla("gold")
        disableVanilla("copper")
        disableVanilla("coal", "")
        disableVanilla("diamond", "")
        disableVanilla("redstone", "")
        disableVanilla("lapis", "lazuli")
        disableVanilla("emerald", "")

        // smelt iron nugget to wrought iron
        vanilla {
            smelting(getMaterial("iron").tag("nugget"),
                getMaterial("wrought_iron").item("nugget"),
                200, suffix = "_from_iron")
        }

        // freeze water and air
        vacuumFreezer {
            defaults {
                voltage(Voltage.MV)
            }
            output("air", "liquid") {
                input("air")
                workTicks(200)
            }
            output("water", "liquid") {
                input("water", "gas")
                workTicks(32)
            }
        }

        generateStone()
        stone()
        tags()
    }

    private fun blast() {
        blastFurnace {
            defaults {
                voltage(Voltage.LV)
                workTicks(400)
                extra {
                    temperature(2000)
                }
            }
            input("chalcopyrite", "dust", 2) {
                input("oxygen", "gas", 9)
                output("iron", "ingot", 3)
                output("copper", "ingot", 3)
                output("sulfuric_acid", "gas", 6)
            }
            input("pyrite", "dust", 2) {
                input("oxygen", "gas", 4.5)
                output("iron", "ingot", 3)
                output("sulfuric_acid", "gas", 3)
            }
            input("limonite", "dust", 8) {
                input("carbon", "dust", 9)
                output("iron", "ingot", 12)
                output("carbon_dioxide", "gas", 9)
                workTicks(1600)
            }
            input("banded_iron", "dust", 8) {
                input("carbon", "dust", 9)
                output("iron", "ingot", 12)
                output("carbon_dioxide", "gas", 9)
                workTicks(1600)
            }
            input("garnierite", "dust", 4) {
                input("carbon", "dust", 3)
                output("nickel", "ingot", 6)
                output("carbon_dioxide", "gas", 3)
                workTicks(800)
            }
            input("cassiterite", "dust", 2) {
                input("carbon", "dust", 3)
                output("tin", "ingot", 3)
                output("carbon_dioxide", "gas", 3)
            }
            input("galena", "dust", 2) {
                input("oxygen", "gas", 4.5)
                output("lead", "ingot", 3)
                output("antimony", "ingot", 1)
                output("sulfuric_acid", "gas", 3)
            }
            input("sphalerite", "dust", 2) {
                input("oxygen", "gas", 4.5)
                output("zinc", "ingot", 3)
                output("silver", "ingot", 1)
                output("sulfuric_acid", "gas", 3)
            }
        }
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
            for (variant in OreVariant.entries) {
                output(variant.baseItem) {
                    if (variant == OreVariant.STONE) {
                        voltage(Voltage.PRIMITIVE)
                    } else {
                        voltage(variant.voltage)
                    }
                }
            }
            output("water", "liquid") {
                voltage(Voltage.ULV)
            }
        }
        stoneGenerator {
            defaults {
                voltage(Voltage.MV)
            }
            output("air", "gas")
            output("sea_water", "liquid")
        }
    }

    private fun stone() {
        // stone -> gravel
        toolCrafting(Items.GRAVEL) {
            pattern("#")
            pattern("#")
            define('#', getMaterial("stone").tag("block"))
            toolTag(TOOL_HAMMER)
        }
        // gravel -> flint
        toolCrafting("flint", "primary") {
            pattern("###")
            define('#', Items.GRAVEL)
            toolTag(TOOL_HAMMER)
        }
        // gravel -> sand
        toolShapeless(Items.GRAVEL, Items.SAND, TOOL_MORTAR)

        // macerate stones
        macerator {
            defaults {
                voltage(Voltage.LV)
            }
            output("silicon_dioxide", "dust") {
                input("glass", "primary")
                workTicks(128)
            }
            output("silicon_dioxide", "dust", suffix = "_from_flint") {
                input("flint", "primary")
                workTicks(128)
            }
            output(Items.SAND) {
                input(Items.GRAVEL)
                workTicks(64)
            }
            output(Items.SAND, suffix = "_from_sandstone") {
                input(Items.SANDSTONE)
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
            input("stone", "block", 2) {
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
                workTicks(400)
            }
        }
    }

    private fun tags() {
        DATA_GEN.apply {
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
