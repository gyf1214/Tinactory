package org.shsts.tinactory.datagen.content.material

import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllItems
import org.shsts.tinactory.content.AllItems.FERTILIZER
import org.shsts.tinactory.content.AllItems.RUBBER_LEAVES
import org.shsts.tinactory.content.AllItems.RUBBER_LOG
import org.shsts.tinactory.content.AllItems.RUBBER_SAPLING
import org.shsts.tinactory.content.AllRecipes
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper
import org.shsts.tinactory.datagen.content.RegistryHelper
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolShapeless
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla

object Woods {
    fun init() {
        vanilla("oak")
        vanilla("spruce")
        vanilla("birch")
        vanilla("jungle")
        vanilla("acacia")
        vanilla("dark_oak")
        vanilla("crimson")
        vanilla("warped")

        farm(RUBBER_SAPLING.get(), RUBBER_LOG.get(), RUBBER_LEAVES.get(), true)

        misc()
    }

    private fun farm(sapling: ItemLike, log: ItemLike, leaves: ItemLike, isRubber: Boolean) {
        val stickResin = AllItems.STICKY_RESIN.get()
        autofarm {
            defaults {
                output(log, 6)
                if (isRubber) {
                    output(stickResin, 6)
                }
                output(sapling, 2)
                voltage(Voltage.LV)
            }
            input(sapling) {
                input("biomass", "fluid")
                workTicks(1600)
            }
            input(sapling, suffix = "_with_bone_meal") {
                input("water", "fluid")
                input(Items.BONE_MEAL, 2, port = 2)
                output(leaves, 16)
                workTicks(300)
            }
        }
        autofarm {
            input(sapling, suffix = "_with_fertilizer") {
                input("water", "fluid")
                input(FERTILIZER.get(), 2, port = 2)
                output(log, 12)
                if (isRubber) {
                    output(stickResin, 12)
                }
                output(sapling, 4)
                output(leaves, 32)
                voltage(Voltage.MV)
                workTicks(300)
            }
        }
    }

    private fun vanilla(prefix: String) {
        val nether = prefix == "crimson" || prefix == "warped"

        val planks = RegistryHelper.vanillaItem("${prefix}_planks")
        val logsTag = AllTags.item(LocHelper.mcLoc(prefix + if (nether) "_stems" else "_logs"))
        val wood = prefix + if (nether) "_hyphae" else "_wood"
        val woodStripped = "stripped_$wood"

        // logs to planks
        vanilla(replace = true) {
            nullRecipe(wood, woodStripped)
            shapeless(logsTag, planks, toAmount = 2)
        }
        toolShapeless(logsTag, planks, AllTags.TOOL_SAW, amount = 4)

        // wood components
        val sign = RegistryHelper.vanillaItem("${prefix}_sign")
        val pressurePlate = RegistryHelper.vanillaItem("${prefix}_pressure_plate")
        val button = RegistryHelper.vanillaItem("${prefix}_button")
        val slab = RegistryHelper.vanillaItem("${prefix}_slab")
        vanilla {
            nullRecipe(sign, pressurePlate, button, slab)
        }
        toolShapeless(planks, slab, AllTags.TOOL_SAW, amount = 2)
        toolShapeless(pressurePlate, button, AllTags.TOOL_SAW, amount = 4)
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
            val sapling = RegistryHelper.vanillaItem("${prefix}_sapling")
            val log = RegistryHelper.vanillaItem("${prefix}_log")
            val leaves = RegistryHelper.vanillaItem("${prefix}_leaves")
            farm(sapling, log, leaves, false)
        }
    }

    private fun misc() {
        // stick
        toolCrafting(Items.STICK, 4) {
            pattern("#")
            pattern("#")
            define('#', ItemTags.PLANKS)
            toolTag(AllTags.TOOL_SAW)
        }
        vanilla(replace = true) {
            shaped(Items.STICK, 2) {
                pattern("#")
                pattern("#")
                define('#', ItemTags.PLANKS)
                unlockedBy("has_planks", AllRecipes.has(ItemTags.PLANKS))
            }
        }
        lathe {
            output(Items.STICK, 2) {
                input(ItemTags.PLANKS)
                voltage(Voltage.LV)
                workTicks(32)
            }
        }

        // to biomass
        extractor {
            defaults {
                voltage(Voltage.MV)
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
}
