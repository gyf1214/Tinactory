package org.shsts.tinactory.datagen.content.material

import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllItems.FERTILIZER
import org.shsts.tinactory.content.AllItems.RUBBER_LEAVES
import org.shsts.tinactory.content.AllItems.RUBBER_LOG
import org.shsts.tinactory.content.AllItems.RUBBER_SAPLING
import org.shsts.tinactory.content.AllItems.STICKY_RESIN
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllRecipes.has
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_MORTAR
import org.shsts.tinactory.content.AllTags.TOOL_SAW
import org.shsts.tinactory.content.AllTags.TOOL_SHEARS
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.RubberLogBlock
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItem
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.alloySmelter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.pyrolyseOven
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolShapeless
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

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

        rubber()
        misc()
    }

    private fun farm(sapling: ItemLike, log: ItemLike, leaves: ItemLike, isRubber: Boolean) {
        val stickResin = STICKY_RESIN.get()
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
                input("biomass")
                workTicks(1600)
            }
            input(sapling, suffix = "_with_bone_meal") {
                input("water")
                input(Items.BONE_MEAL, 2, port = 2)
                output(leaves, 16)
                workTicks(300)
            }
        }
        autofarm {
            input(sapling, suffix = "_with_fertilizer") {
                input("water")
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
                input("water", amount = 0.6)
                workTicks(240)
            }
            output(slab, 2) {
                input(planks)
                input("water", amount = 0.1)
                workTicks(80)
            }
            output(button, 8) {
                input(pressurePlate)
                input("water", amount = 0.05)
                workTicks(64)
            }
        }

        // farm
        if (!nether) {
            val sapling = vanillaItem("${prefix}_sapling")
            val log = vanillaItem("${prefix}_log")
            val leaves = vanillaItem("${prefix}_leaves")
            farm(sapling, log, leaves, false)
        }
    }

    private fun rubber() {
        DATA_GEN.apply {
            block(RUBBER_LOG).apply {
                blockState { ctx ->
                    ctx.provider().axisBlock(ctx.`object`(),
                        gregtech("blocks/wood/rubber/log_rubber_side"),
                        gregtech("blocks/wood/rubber/log_rubber_top"))
                }
                tag(listOf(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN))
                itemTag(listOf(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN))
                dropSelf()
                dropOnState(STICKY_RESIN, RubberLogBlock.HAS_RUBBER, true)
                build()
            }
            block(RUBBER_LEAVES).apply {
                blockState(Models.cubeTint("wood/rubber/leaves_rubber"))
                tag(BlockTags.LEAVES)
                itemTag(ItemTags.LEAVES)
                dropSelfOnTool(TOOL_SHEARS)
                drop(RUBBER_SAPLING, 0.075f)
                build()
            }
            block(RUBBER_SAPLING).apply {
                blockState { ctx ->
                    val provider = ctx.provider()
                    provider.simpleBlock(ctx.`object`(), provider.models()
                        .cross(ctx.id(), gregtech("blocks/wood/rubber/sapling_rubber")))
                }
                itemModel(Models.basicItem(gregtech("blocks/wood/rubber/sapling_rubber")))
                tag(BlockTags.SAPLINGS)
                itemTag(ItemTags.SAPLINGS)
                build()
            }
        }

        farm(RUBBER_SAPLING.get(), RUBBER_LOG.get(), RUBBER_LEAVES.get(), true)

        toolShapeless(STICKY_RESIN.get(), getMaterial("raw_rubber").item("dust"), TOOL_MORTAR)
        extractor {
            output("raw_rubber", "dust", 3) {
                input(STICKY_RESIN.get())
                voltage(Voltage.LV)
                workTicks(160)
            }
            output("raw_rubber", "dust", suffix = "from_log") {
                input(RUBBER_LOG.get())
                voltage(Voltage.LV)
                workTicks(320)
            }
        }
        alloySmelter {
            output("rubber", "sheet", 3) {
                input("raw_rubber", amount = 3)
                input("sulfur")
                voltage(Voltage.ULV)
                workTicks(300)
            }
        }
    }

    private fun misc() {
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

        // to biomass
        extractor {
            defaults {
                voltage(Voltage.MV)
            }
            input(ItemTags.LEAVES, 16) {
                output("biomass", amount = 0.3)
                workTicks(128)
            }
            input(ItemTags.SAPLINGS, 16) {
                output("biomass", amount = 0.1)
                workTicks(64)
            }
        }

        // charcoal
        pyrolyseOven {
            defaults {
                voltage(Voltage.LV)
                input(ItemTags.LOGS_THAT_BURN, 16)
                output("creosote_oil", amount = 4)
            }
            output("charcoal", amount = 16) {
                workTicks(1280)
            }
            output("charcoal", amount = 16, suffix = "_with_nitrogen") {
                input("nitrogen", amount = 4)
                workTicks(320)
            }
        }
    }
}
