package org.shsts.tinactory.datagen.content

import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllItems.FERTILIZER
import org.shsts.tinactory.content.AllItems.RUBBER_LEAVES
import org.shsts.tinactory.content.AllItems.RUBBER_LOG
import org.shsts.tinactory.content.AllItems.RUBBER_SAPLING
import org.shsts.tinactory.content.AllItems.STICKY_RESIN
import org.shsts.tinactory.content.AllRecipes.has
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_SAW
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItem
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder.Companion.material
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolShapeless
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.model.IconSet.BRIGHT
import org.shsts.tinactory.datagen.content.model.IconSet.DULL
import org.shsts.tinactory.datagen.content.model.IconSet.FINE
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_HORIZONTAL
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_VERTICAL
import org.shsts.tinactory.datagen.content.model.IconSet.LIGNITE
import org.shsts.tinactory.datagen.content.model.IconSet.METALLIC
import org.shsts.tinactory.datagen.content.model.IconSet.QUARTZ
import org.shsts.tinactory.datagen.content.model.IconSet.ROUGH
import org.shsts.tinactory.datagen.content.model.IconSet.RUBY
import org.shsts.tinactory.datagen.content.model.IconSet.SHINY

object Materials {
    fun init() {
        woods()
        elements()
        firstDegrees()
        higherDegrees()
        ores()
        misc()
    }

    private fun woods() {
        woodRecipe("oak")
        woodRecipe("spruce")
        woodRecipe("birch")
        woodRecipe("jungle")
        woodRecipe("acacia")
        woodRecipe("dark_oak")
        woodRecipe("crimson")
        woodRecipe("warped")
        woodFarmRecipe(RUBBER_SAPLING.get(), RUBBER_LOG.get(), RUBBER_LEAVES.get(), true)

        // disable iron and wood tools
        vanilla {
            nullRecipe(
                Items.WOODEN_AXE,
                Items.WOODEN_HOE,
                Items.WOODEN_PICKAXE,
                Items.WOODEN_SHOVEL,
                Items.WOODEN_SWORD,
                Items.IRON_AXE,
                Items.IRON_HOE,
                Items.IRON_PICKAXE,
                Items.IRON_SHOVEL,
                Items.IRON_SWORD,
                Items.COMPOSTER,
            )
        }

        // stick
        toolCrafting(Items.STICK, 4) {
            pattern("#")
            pattern("#")
            define('#', ItemTags.PLANKS)
            toolTag(TOOL_SAW)
        }
        vanilla(replace = true) {
            shaped(Items.STICK, 2) {
                pattern("#")
                pattern("#")
                define('#', ItemTags.PLANKS)
                unlockedBy("has_planks", has(ItemTags.PLANKS))
            }
        }
        lathe {
            output(Items.STICK, 2) {
                input(ItemTags.PLANKS)
                voltage(Voltage.LV)
                workTicks(32)
            }
        }

        extractor {
            defaults {
                voltage(Voltage.LV)
            }
            input(ItemTags.LEAVES, 16) {
                output("biomass", "fluid", 0.3)
                workTicks(128)
            }
            input(ItemTags.SAPLINGS, 16) {
                output("biomass", "fluid", 0.1)
                workTicks(64)
            }
        }
    }

    private fun elements() {
        material("iron", METALLIC) {
            toolProcess()
            smelt()
        }
        material("gold", SHINY) {
            toolProcess()
            smelt()
            oreProcess {
                byProducts("silver", "nickel", "silver")
            }
        }
        material("copper", SHINY) {
            toolProcess(0.75)
            smelt()
        }
        material("tin", DULL) {
            toolProcess(0.75)
            smelt()
            oreProcess()
        }
        material("sulfur", DULL)
        material("cadmium", SHINY)
        material("cobalt", METALLIC) {
            toolProcess(1.25)
            smelt()
        }
        material("nickel", METALLIC) {
            toolProcess(1.25)
            smelt()
        }
        material("magnesium", METALLIC) {
            machineProcess(Voltage.LV)
        }
        material("thorium", SHINY)
        material("chrome", SHINY) {
            machineProcess(Voltage.MV, 1.5)
            blast(Voltage.MV, 2200, 1024) {
                component("nitrogen")
            }
        }
        material("antimony", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("silver", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
            oreProcess {
                byProducts("antimony", "antimony", "gallium")
            }
        }
        material("vanadium", METALLIC)
        material("aluminium", DULL) {
            machineProcess(Voltage.LV)
            blast(Voltage.LV, 1500, 400)
        }
        material("lead", DULL) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("zinc", METALLIC) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("gallium", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("carbon", DULL) {
            machineProcess(Voltage.HV)
        }
        material("manganese", DULL)
        material("arsenic", DULL)
        material("silicon", METALLIC)
        material("beryllium", METALLIC) {
            machineProcess(Voltage.LV, 0.6)
            smelt()
        }
        material("sodium", METALLIC)
        material("potassium", METALLIC)
        material("calcium", METALLIC)
        material("lithium", DULL)
        material("titanium", METALLIC) {
            machineProcess(Voltage.HV, 1.25)
            blast(Voltage.HV, 2000, 960) {
                component("nitrogen")
            }
        }
        material("neodymium", METALLIC) {
            machineProcess(Voltage.HV)
            blast(Voltage.MV, 1300, 2400)
        }
    }

    private fun firstDegrees() {
        material("wrought_iron", METALLIC) {
            toolProcess()
            smelt()
        }
        material("bronze", METALLIC) {
            toolProcess(0.75)
            smelt()
            alloy(Voltage.ULV) {
                component("copper", 3)
                component("tin")
            }
        }
        material("cobaltite", METALLIC) {
            smelt("cobalt")
            centrifuge(Voltage.LV) {
                component("cobalt")
                component("arsenic")
                component("sulfur")
            }
        }
        material("invar", METALLIC) {
            toolProcess(1.25)
            smelt()
            alloy(Voltage.ULV) {
                component("iron", 2)
                component("nickel")
            }
        }
        material("cupronickel", METALLIC) {
            toolProcess(1.25)
            smelt()
            alloy(Voltage.ULV) {
                component("copper")
                component("nickel")
            }
        }
        material("steel", METALLIC) {
            toolProcess(1.5)
            blast(Voltage.ULV, 1000, 800)
            blast(Voltage.ULV, 1000, 1000, "iron")
            blast(Voltage.MV, 1000, 96, "wrought_iron") {
                component("oxygen")
            }
        }
        material("red_alloy", DULL) {
            toolProcess(0.5)
            smelt()
            alloyOnly(Voltage.ULV) {
                amount(1)
                component("copper")
                component("redstone", 4)
            }
        }
        material("battery_alloy", DULL) {
            machineProcess(Voltage.LV)
            smelt()
            alloy(Voltage.LV) {
                component("lead", 4)
                component("antimony")
            }
        }
        material("soldering_alloy", DULL) {
            fluidAlloy(Voltage.LV) {
                component("tin", 6)
                component("lead", 3)
                component("antimony")
            }
        }
        material("rutile", SHINY)
        material("brass", METALLIC) {
            toolProcess(0.75)
            smelt()
            alloy(Voltage.LV) {
                component("copper", 3)
                component("zinc")
            }
        }
        material("gallium_arsenide", DULL) {
            mix(Voltage.LV) {
                component("gallium")
                component("arsenic")
            }
        }
        material("kanthal", METALLIC) {
            mix(Voltage.LV) {
                component("iron")
                component("aluminium")
                component("chrome")
            }
            machineProcess(Voltage.LV)
            blast(Voltage.LV, 1800, 1000)
        }
        material("electrum", SHINY) {
            machineProcess(Voltage.LV, 0.75)
            smelt()
            alloy(Voltage.LV) {
                component("gold")
                component("silver")
            }
        }
        material("stainless_steel", SHINY) {
            machineProcess(Voltage.MV)
            blast(Voltage.MV, 1700, 1100)
            mix(Voltage.MV) {
                component("iron", 6)
                component("nickel")
                component("manganese")
                component("chrome")
            }
        }

        material("sodium_chloride", FINE)
        material("potassium_chloride", FINE)
        material("magnesium_chloride", DULL)
        material("calcium_chloride", FINE)
        material("lithium_chloride", FINE)
        material("ammonium_chloride", DULL)
        material("sodium_carbonate", DULL)
        material("potassium_carbonate", DULL)
        material("calcium_carbonate", DULL)
        material("lithium_carbonate", DULL)
        material("sodium_sulfate", DULL)
        material("potassium_nitrate", FINE)
        material("sodium_hydroxide", DULL)
        material("calcium_hydroxide", DULL)
        material("sulfuric_acid", DULL) {
            fluidMix(Voltage.MV) {
                component("sulfuric_acid", sub = "dilute")
                component("water")
            }
        }
        material("nickel_zinc_ferrite", METALLIC) {
            machineProcess(Voltage.MV, 1.25)
            blast(Voltage.MV, 1500, 400) {
                component("oxygen", 2)
            }
            mix(Voltage.MV) {
                component("iron", 4)
                component("nickel")
                component("zinc")
            }
        }

        material("coke", LIGNITE) {
            toolProcess()
        }
        material("charcoal", FINE) {
            toolProcess()
        }
        material("silicon_dioxide", QUARTZ) {
            smelt("glass", "primary")
        }
        material("nichrome", METALLIC) {
            machineProcess(Voltage.MV, 1.25)
            blast(Voltage.HV, 2700, 880) {
                component("nitrogen")
            }
            mix(Voltage.MV) {
                component("nickel", 4)
                component("chrome")
            }
        }
        material("aluminium_oxide", FINE)
        material("annealed_copper", BRIGHT) {
            machineProcess(Voltage.MV, 0.8)
        }
    }

    private fun higherDegrees() {
        material("cobalt_brass", METALLIC) {
            machineProcess(Voltage.LV, 2.0)
            smelt()
            mix(Voltage.LV) {
                component("brass", 7)
                component("aluminium")
                component("cobalt")
            }
        }
        material("salt_water", DULL) {
            fluidMix(Voltage.MV) {
                component("sodium_chloride")
                component("water")
            }
        }
        material("pe", DULL) {
            machineProcess(Voltage.LV, 0.5)
        }
        material("pvc", DULL) {
            machineProcess(Voltage.LV, 0.5)
        }
        material("lpg", DULL) {
            fluidMix(Voltage.MV) {
                component("ethane")
                component("propane")
            }
        }
        material("diesel", DULL) {
            fluidMix(Voltage.MV) {
                component("light_fuel", 5)
                component("heavy_fuel")
            }
        }
        material("vanadium_steel", METALLIC) {
            machineProcess(Voltage.MV, 1.5)
            blast(Voltage.MV, 2500, 1280) {
                component("nitrogen")
            }
        }
        material("ptfe", DULL) {
            machineProcess(Voltage.LV, 0.75)
        }
    }

    private fun ores() {
        material("chalcopyrite", DULL) {
            oreProcess {
                primitive = true
                byProducts("sulfur", "cobaltite", "zinc")
            }
            smelt("copper")
        }
        material("pyrite", ROUGH) {
            oreProcess {
                primitive = true
                byProducts("sulfur", "copper", "cadmium")
            }
            smelt("iron")
        }
        material("limonite", METALLIC) {
            oreProcess {
                byProducts("nickel", "copper", "nickel")
            }
            smelt("iron")
        }
        material("banded_iron", DULL) {
            oreProcess {
                byProducts("nickel", "copper", "nickel")
            }
            smelt("iron")
        }
        material("garnierite", METALLIC) {
            oreProcess {
                byProducts("magnesium_chloride", "magnesium_chloride", "nickel")
            }
            smelt("nickel")
        }
        material("coal", DULL) {
            toolProcess()
            oreProcess {
                amount = 2
                siftAndHammer = true
                byProducts("coal", "coal", "thorium")
            }
        }
        material("cassiterite", METALLIC) {
            oreProcess {
                amount = 2
                byProducts("tin")
            }
            smelt("tin")
        }
        material("redstone", DULL) {
            oreProcess {
                amount = 5
                byProducts("glowstone", "glowstone", "rare_earth")
            }
            centrifuge(Voltage.LV) {
                component("pyrite", 6)
                component("ruby", 3)
                component("silicon")
            }
        }
        material("cinnabar", SHINY) {
            oreProcess {
                byProducts("rare_earth", "glowstone", "rare_earth")
            }
        }
        material("ruby", RUBY) {
            machineProcess(Voltage.LV, 2.0)
            oreProcess {
                siftAndHammer = true
                byProducts("chrome")
            }
        }
        material("magnetite", METALLIC) {
            oreProcess {
                byProducts("gold", "vanadium", "copper")
            }
            smelt("iron")
        }
        material("galena", DULL) {
            oreProcess {
                byProducts("sulfur", "antimony", "silver")
            }
            smelt("lead")
        }
        material("sphalerite", DULL) {
            oreProcess {
                byProducts("sulfur", "silver", "gallium")
            }
            smelt("zinc")
        }
        material("graphite", DULL) {
            oreProcess {
                byProducts("carbon")
            }
        }
        material("diamond", SHINY) {
            oreProcess {
                siftAndHammer = true
                byProducts("carbon")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("bauxite", DULL) {
            oreProcess {
                byProducts("aluminium", "gallium", "rutile")
            }
        }
        material("ilmenite", METALLIC) {
            oreProcess {
                byProducts("manganese", "manganese", "rutile")
            }
        }
        material("natural_gas", DULL) {
            oilOre(192)
        }
        material("light_oil", DULL) {
            oilOre(240)
        }
        material("heavy_oil", DULL) {
            oilOre(512)
        }
        material("emerald", SHINY) {
            oreProcess {
                siftAndHammer = true
                byProducts("aluminium", "beryllium", "thorium")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("sapphire", GEM_VERTICAL) {
            oreProcess {
                siftAndHammer = true
                byProducts("aluminium", "rutile", "rutile")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("topaz", GEM_HORIZONTAL) {
            oreProcess {
                byProducts("blue_topaz", "aluminium", "blue_topaz")
            }
        }
        material("blue_topaz", GEM_HORIZONTAL) {
            oreProcess {
                byProducts("topaz", "aluminium", "topaz")
            }
        }

        blastOres()
    }

    private fun woodFarmRecipe(sapling: ItemLike, log: ItemLike, leaves: ItemLike, isRubber: Boolean) {
        val stickResin = STICKY_RESIN.get()
        autofarm {
            defaults {
                voltage(Voltage.LV)
            }
            input(sapling) {
                input("biomass", "fluid", 1)
                output(log, 6)
                if (isRubber) {
                    output(stickResin, 6)
                }
                output(sapling, 2)
                workTicks(1600)
            }
            input(sapling, suffix = "_with_bone_meal") {
                input("water", "fluid", 1)
                input(Items.BONE_MEAL, 2, port = 2)
                output(log, 6)
                if (isRubber) {
                    output(stickResin, 6)
                }
                output(sapling, 2)
                output(leaves, 16)
                workTicks(300)
            }
            input(sapling, 1) {
                input("water", "fluid", 1f)
                input(FERTILIZER.get(), 2)
                output(log, 12)
                if (isRubber) {
                    output(stickResin, 12)
                }
                output(sapling, 4)
                output(leaves, 32)
                workTicks(300)
            }
        }
    }

    private fun woodRecipe(prefix: String) {
        val nether = prefix == "crimson" || prefix == "warped"

        val planks = vanillaItem("${prefix}_planks")
        val logsTag = AllTags.item(mcLoc(prefix + if (nether) "_stems" else "_logs"))
        val wood = prefix + if (nether) "_hyphae" else "_wood"
        val woodStripped = "stripped_$wood"

        // logs to planks
        vanilla(replace = true) {
            nullRecipe(wood, woodStripped)
            shapeless(logsTag, planks, toAmount = 2)
        }
        toolShapeless(logsTag, planks, TOOL_SAW, amount = 4)

        // wood components
        val sign = vanillaItem("${prefix}_sign")
        val pressurePlate = vanillaItem("${prefix}_pressure_plate")
        val button = vanillaItem("${prefix}_button")
        val slab = vanillaItem("${prefix}_slab")
        vanilla {
            nullRecipe(sign, pressurePlate, button, slab)
        }
        toolShapeless(planks, slab, TOOL_SAW, amount = 2)
        toolShapeless(pressurePlate, button, TOOL_SAW, amount = 4)
        cutter {
            defaults {
                voltage(Voltage.LV)
            }
            output(planks, 6) {
                input(logsTag)
                input("water", "liquid", 0.6)
                workTicks(240)
            }
            output(slab, 2) {
                input(planks)
                input("water", "liquid", 0.1)
                workTicks(80)
            }
            output(button, 8) {
                input(pressurePlate)
                input("water", "liquid", 0.05)
                workTicks(64)
            }
        }

        // farm
        if (!nether) {
            val sapling = vanillaItem("${prefix}_sapling")
            val log = vanillaItem("${prefix}_log")
            val leaves = vanillaItem("${prefix}_leaves")
            woodFarmRecipe(sapling, log, leaves, false)
        }
    }

    private fun blastOres() {
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

    private fun misc() {
        material("test", DULL)
        material("stone", ROUGH) {
            toolProcess()
        }
        material("flint", DULL) {
            toolProcess()
        }
        material("raw_rubber", DULL)
        material("rubber", SHINY) {
            toolProcess()
        }
        material("glowstone", SHINY)
        material("rare_earth", ROUGH)
        material("glass", SHINY)
    }
}
