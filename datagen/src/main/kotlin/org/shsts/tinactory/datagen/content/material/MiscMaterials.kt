package org.shsts.tinactory.datagen.content.material

import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllTags.FLUID_STORAGE_CELL
import org.shsts.tinactory.AllTags.ITEM_STORAGE_CELL
import org.shsts.tinactory.AllTags.ORE_BASE_DUST
import org.shsts.tinactory.AllTags.PATTERN_STORAGE_CELL
import org.shsts.tinactory.AllTags.STORAGE_CELL
import org.shsts.tinactory.AllTags.TOOL
import org.shsts.tinactory.AllTags.TOOL_FILE
import org.shsts.tinactory.AllTags.TOOL_HAMMER
import org.shsts.tinactory.AllTags.TOOL_HANDLE
import org.shsts.tinactory.AllTags.TOOL_MORTAR
import org.shsts.tinactory.AllTags.TOOL_SAW
import org.shsts.tinactory.AllTags.TOOL_SCREW
import org.shsts.tinactory.AllTags.TOOL_SCREWDRIVER
import org.shsts.tinactory.AllTags.TOOL_SHEARS
import org.shsts.tinactory.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.AllTags.TOOL_WRENCH
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.cubeTint
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.itemKey
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.DataFactories.itemData
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.bacteriaVat
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.distillation
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.fusionReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.implosionCompressor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.stoneGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.wiremill
import org.shsts.tinactory.integration.material.OreVariant

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
            nullRecipe(
                "quartz", "quartz_from_blasting", "quartz_block",
                "netherite_block", "netherite_ingot", "netherite_ingot_from_netherite_block",
                "netherite_scrap", "netherite_scrap_from_blasting",
                Items.NETHER_WART_BLOCK, Items.SLIME_BLOCK, Items.SLIME_BALL)
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
            output("nitrogen", "liquid") {
                input("nitrogen")
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

        blastFurnace {
            recipe("material/rhodium_plated_palladium/ingot_hot_from_raw") {
                input(getItem("component/raw_rhodium_plated_palladium"))
                input("argon")
                output("rhodium_plated_palladium", "ingot_hot")
                voltage(Voltage.IV)
                workTicks(1280)
                temperature(4500)
            }
        }

        bacteriaVat {
            output("bacteria_culture", amount = 0.01, suffix = "_from_biomass") {
                input("biomass", amount = 0.1)
                input("nuclear_waste", "slurry", 0.01)
                voltage(Voltage.HV)
                requireCleanness(0.7, 1.0)
            }
            output("bacteria_culture", amount = 0.002) {
                input("bacteria_culture", amount = 0.001)
                input("sterile_growth_medium", amount = 0.001)
                voltage(Voltage.LV)
                requireCleanness(0.5, 0.8)
            }
            output("cultivated_bacteria", amount = 0.01) {
                input("bacteria_culture", amount = 0.1)
                input("radon", amount = 0.01)
                voltage(Voltage.HV)
                requireCleanness(0.85, 1.35)
            }
            output("cultivated_bacteria", amount = 0.02, suffix = "_from_reproduction") {
                input("cultivated_bacteria", amount = 0.001)
                input("enriched_growth_medium", amount = 0.001)
                voltage(Voltage.MV)
                requireCleanness(0.6, 0.85)
            }
            output("advanced_bacteria", amount = 0.01) {
                input("cultivated_bacteria", amount = 0.1)
                input("naquadria", "molten", 1f / 72f)
                voltage(Voltage.IV)
                requireCleanness(0.9, 1.4)
            }
        }

        fusionReactor {
            output("naquadria_infused_rocket_fuel", "plasma") {
                input("rocket_fuel", "liquid")
                input("naquadria", "molten")
                voltage(Voltage.ZPM)
                workTicks(200)
            }
            output("netherite", "plasma", 0.25) {
                input("netherite_scrap", "molten")
                input("gold", "molten")
                voltage(Voltage.LUV)
                workTicks(128)
            }
            output("nether_star", "plasma", 0.125) {
                input("enriched_naquadah", "molten", 0.125)
                input("wither_matrix", "liquid", 0.125)
                voltage(Voltage.LUV)
                workTicks(64)
            }
            output("activated_naquadah", "plasma") {
                input("naquadah", "molten")
                input("hydrogen", "gas", 0.125)
                voltage(Voltage.LUV)
                workTicks(256)
            }
            output("neutronium", "plasma", 1f / 144f) {
                input("nether_star", "molten", 1f / 36f)
                input("naquadria", "molten", 1f / 36f)
                voltage(Voltage.ZPM)
                workTicks(96)
            }
        }

        vanilla {
            nullRecipe(
                "bone_meal", "bone_meal_from_bone_block", "bone_block",
                Items.MAGMA_BLOCK, Items.MAGMA_CREAM, Items.FIRE_CHARGE,
                Items.LEATHER, Items.CLAY, Items.MUDDY_MANGROVE_ROOTS, Items.SNOW, Items.SNOW_BLOCK)
        }

        macerator {
            defaults {
                voltage(Voltage.LV)
                workTicks(128)
            }
            output(Items.BONE_MEAL, 3) {
                input(Items.BONE)
            }
            output(Items.BONE_MEAL, 9, suffix = "_from_bone_block") {
                input(Items.BONE_BLOCK)
            }
            output(Items.GLOWSTONE_DUST, 4) {
                input(Items.GLOWSTONE)
            }
        }

        implosionCompressor {
            output(Items.BONE_BLOCK, 2) {
                input(Items.BONE_MEAL, 18)
                input(Items.TNT, port = 1)
                voltage(Voltage.LV)
            }
            output(Items.SLIME_BLOCK, 2) {
                input(Items.SLIME_BALL, 18)
                input(Items.TNT, port = 1)
                voltage(Voltage.HV)
            }
        }

        cutter {
            output(Items.BONE, 3) {
                input(Items.BONE_BLOCK)
                input("water", amount = 0.05)
                voltage(Voltage.LV)
                workTicks(64)
            }
            output(Items.MAGMA_CREAM, 4) {
                input(Items.MAGMA_BLOCK)
                input("water", amount = 0.1)
                voltage(Voltage.LV)
                workTicks(128)
            }
            output(Items.SNOW, 2) {
                input(Items.SNOW_BLOCK)
                input("water", amount = 0.1)
                voltage(Voltage.LV)
                workTicks(64)
            }
        }

        centrifuge {
            input(Items.MAGMA_CREAM) {
                output(Items.SLIME_BALL)
                output("lava", amount = 0.2)
                voltage(Voltage.MV)
                workTicks(120)
            }
        }

        mixer {
            defaults {
                voltage(Voltage.MV)
            }
            output(Items.MAGMA_CREAM) {
                input(Items.SLIME_BALL)
                input("lava", amount = 0.2)
                workTicks(40)
            }
            output(Items.FIRE_CHARGE, 3) {
                input(Items.GUNPOWDER)
                input(Items.BLAZE_POWDER)
                input(ItemTags.COALS)
                workTicks(192)
            }
        }

        mixer {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
            }
            output(Items.CLAY) {
                input(Items.CLAY_BALL, 4)
            }
            output(Items.MUDDY_MANGROVE_ROOTS) {
                input(Items.MUD)
                input(Items.MANGROVE_ROOTS)
            }
            output(Items.SNOWBALL, 2) {
                input(Items.ICE)
                input("water", amount = 0.1)
            }
            output(Items.SNOW_BLOCK) {
                input(Items.SNOWBALL, 4)
            }
        }

        wiremill {
            output(Items.STRING) {
                input("rubber", "foil")
                voltage(Voltage.LV)
                workTicks(32)
            }
        }

        chemicalReactor {
            output(Items.LEATHER) {
                input(Items.STRING)
                input("pvc", amount = 0.5)
                voltage(Voltage.MV)
                workTicks(96)
                tech(Technologies.ORGANIC_CHEMISTRY)
            }
        }

        generateStone()
        stone()
        naquadahProcessing()
        tags()
    }

    private fun naquadahProcessing() {
        distillation {
            input("activated_naquadah", "plasma", 4) {
                output("enriched_naquadah", "molten")
                output("unstable_naquadria", "plasma")
                output("trinium_residue")
                output("naquadah_residue")
                voltage(Voltage.IV)
                workTicks(1000)
            }
        }

        sifter {
            input("naquadah_residue", "dust") {
                output("naquadah", "dust", rate = 0.8)
                output("titanium", "dust", rate = 0.2)
                voltage(Voltage.IV)
                workTicks(400)
            }
        }

        chemicalReactor {
            output("acidic_naquadria_solution") {
                input("unstable_naquadria")
                input("hydrogen_fluoride", amount = 4)
                voltage(Voltage.IV)
                workTicks(800)
            }
        }

        centrifuge {
            input("acidic_naquadria_solution") {
                output("naquadria_concentrate")
                output("enriched_naquadah", "dust", rate = 0.2)
                voltage(Voltage.IV)
                workTicks(1200)
            }
        }

        blastFurnace {
            output("naquadria", "ingot_hot", suffix = "_from_concentrate") {
                input("naquadria_concentrate")
                input("potassium", amount = 2)
                output("potassium_bifluoride", amount = 2)
                voltage(Voltage.LUV)
                workTicks(2400)
                temperature(5400)
            }
            output("trinium", "ingot_hot", suffix = "_from_sulfide") {
                input("trinium_sulfide")
                input("hydrogen")
                output("sulfur")
                voltage(Voltage.LUV)
                workTicks(2400)
                temperature(5400)
            }
        }

        chemicalReactor {
            output("trinium_sulfide") {
                input("trinium_residue")
                input("sulfuric_acid", amount = 2)
                output("rarest_metallic")
                output("sulfuric_acid", "dilute", 2)
                voltage(Voltage.IV)
                workTicks(800)
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
                    nullRecipe(fullName, "${fullName}_from_waxed_copper_block")
                } else {
                    nullRecipe(
                        "${fullName}_from_${name}_block",
                        "${fullName}_from_nuggets",
                        "${name}_nugget")
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
            tag(itemKey(Items.SHEARS), TOOL_SHEARS)
            tag(itemKey(Items.STICK), TOOL_HANDLE)
            tag(getMaterial("iron").tag("stick"), TOOL_HANDLE)
            tag(getMaterial("iron").tag("screw"), TOOL_SCREW)

            for (base in OreVariant.entries) {
                tag(getMaterial(base.material).tag("dust"), ORE_BASE_DUST)
            }

            tag(ITEM_STORAGE_CELL, STORAGE_CELL)
            tag(FLUID_STORAGE_CELL, STORAGE_CELL)
            tag(PATTERN_STORAGE_CELL, STORAGE_CELL)

            tag(getMaterial("coke").tag("primary"), ItemTags.COALS)
        }
    }
}
