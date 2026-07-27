package org.shsts.tinactory.datagen.content.material

import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllItems.RUBBER_LEAVES
import org.shsts.tinactory.AllItems.RUBBER_LOG
import org.shsts.tinactory.AllItems.RUBBER_SAPLING
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.AllTags.TOOL_HANDLE
import org.shsts.tinactory.AllTags.TOOL_MORTAR
import org.shsts.tinactory.AllTags.TOOL_SAW
import org.shsts.tinactory.AllTags.TOOL_SHEARS
import org.shsts.tinactory.content.material.RubberLogBlock
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.alloySmelter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autofarm
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.pyrolyseOven
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.material.Crops.siftSeed

object Woods {
    fun init() {
        vanilla("oak")
        vanilla("spruce")
        vanilla("birch")
        vanilla("jungle")
        vanilla("acacia")
        vanilla("dark_oak")
        vanilla("cherry")
        vanilla("mangrove", sapling = Items.MANGROVE_PROPAGULE)
        vanilla("crimson", isNether = true, leaves = Items.NETHER_WART_BLOCK)
        vanilla("warped", isNether = true)
        vanilla("bamboo", isWood = false, logId = "bamboo_block", planksRate = 1, boat = Items.BAMBOO_RAFT)

        rubber()
        misc()
    }

    private fun farm(sapling: ItemLike, log: ItemLike, leaves: ItemLike, extra: ItemLike,
        isRubber: Boolean = false) {
        autofarm {
            defaults {
                if (isRubber) {
                    output(log, 6)
                    output(extra, 6)
                } else {
                    output(log, 4)
                    output(extra, 2)
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
                input(getItem("misc/fertilizer"), 2, port = 2)
                if (isRubber) {
                    output(log, 12)
                    output(extra, 12)
                } else {
                    output(log, 8)
                    output(extra, 4)
                }
                output(sapling, 4)
                output(leaves, 32)
                voltage(Voltage.LV)
                workTicks(300)
            }
        }
    }

    private fun components(prefix: String, isNether: Boolean = false, boat: ItemLike? = null) {
        val planks = vanillaItem("${prefix}_planks")
        val slab = vanillaItem("${prefix}_slab")
        val stairs = vanillaItem("${prefix}_stairs")
        val sign = vanillaItem("${prefix}_sign")
        val hangingSign = vanillaItem("${prefix}_hanging_sign")
        val pressurePlate = vanillaItem("${prefix}_pressure_plate")
        val button = vanillaItem("${prefix}_button")
        val fence = vanillaItem("${prefix}_fence")
        val gate = vanillaItem("${prefix}_fence_gate")
        val door = vanillaItem("${prefix}_door")
        val trapdoor = vanillaItem("${prefix}_trapdoor")

        vanilla {
            nullRecipe(slab, stairs)
            nullRecipe(sign, hangingSign, pressurePlate, button)
            nullRecipe(fence, gate, door, trapdoor)
        }

        toolCrafting {
            shapeless(planks, slab, TOOL_SAW, amount = 2)
            shapeless(pressurePlate, button, TOOL_SAW, amount = 4)
        }

        assembler {
            defaults {
                voltage(Voltage.ULV)
                tech(Technologies.SOLDERING)
                workTicks(64)
            }
            output(sign) {
                input(planks)
                input(TOOL_HANDLE)
            }
            output(hangingSign) {
                input(planks)
                input(Items.CHAIN)
            }
            output(pressurePlate) {
                input(slab)
                input("iron", "ring")
                input("redstone", "dust")
                workTicks(128)
            }
            output(fence) {
                input(slab)
                input(TOOL_HANDLE, 2)
            }
            output(gate) {
                input(slab, 2)
                input(TOOL_HANDLE)
                input("redstone", "dust")
            }
            output(door) {
                input(planks, 2)
                input("redstone", "dust")
            }
            output(trapdoor) {
                input(slab, 3)
                input("redstone", "dust")
            }
        }

        cutter {
            defaults {
                voltage(Voltage.LV)
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

        lathe {
            output(stairs) {
                input(slab, 2)
                voltage(Voltage.LV)
                workTicks(80)
            }
        }

        if (!isNether) {
            val boat = boat ?: vanillaItem("${prefix}_boat")
            vanilla {
                nullRecipe(boat)
            }
            assembler {
                output(boat) {
                    input(planks, 2)
                    input(slab)
                    input("rubber", amount = 0.5)
                    voltage(Voltage.LV)
                    workTicks(128)
                    tech(Technologies.HOT_WORKING)
                }
            }
        }
    }

    private fun vanilla(prefix: String,
        isNether: Boolean = false, isWood: Boolean = true, isFarm: Boolean = true,
        logId: String? = null, planksRate: Int = 2, boat: ItemLike? = null,
        sapling: ItemLike? = null, leaves: ItemLike? = null) {
        val planks = vanillaItem("${prefix}_planks")
        val logId = logId ?: (prefix + if (isNether) "_stem" else "_log")
        val log = vanillaItem(logId)
        val logStripped = vanillaItem("stripped_$logId")
        val logsTag = AllTags.item(mcLoc("${logId}s"))

        vanilla(replace = true) {
            shapeless(logsTag, planks, toAmount = planksRate, criteria = "has_logs") {
                group("planks")
            }
        }
        toolCrafting {
            shapeless(logsTag, planks, TOOL_SAW, amount = planksRate * 2)
        }
        cutter {
            output(planks, planksRate * 3) {
                input(logsTag)
                input("water", amount = planksRate * 0.3)
                voltage(Voltage.LV)
                workTicks(240)
            }
        }
        lathe {
            output(logStripped) {
                input(log)
                voltage(Voltage.LV)
                workTicks(120)
            }
        }

        if (isWood) {
            val woodId = prefix + if (isNether) "_hyphae" else "_wood"
            val wood = vanillaItem(woodId)
            val woodStripped = vanillaItem("stripped_$woodId")

            vanilla {
                nullRecipe(wood, woodStripped)
            }
            lathe {
                output(woodStripped) {
                    input(wood)
                    voltage(Voltage.LV)
                    workTicks(120)
                }
            }

            if (isFarm) {
                val sapling = sapling ?: vanillaItem(if (isNether) "${prefix}_fungus" else "${prefix}_sapling")
                val leaves = leaves ?: vanillaItem(if (isNether) "${prefix}_wart_block" else "${prefix}_leaves")
                farm(sapling, log, leaves, wood)
            }
        }

        components(prefix, isNether, boat)
    }

    private fun rubber() {
        val resin = getItem("rubber_tree/sticky_resin")

        blockData {
            block(RUBBER_LOG) {
                blockState { ctx ->
                    ctx.provider().axisBlock(ctx.`object`(),
                        gregtech("block/wood/rubber/log_rubber_side"),
                        gregtech("block/wood/rubber/log_rubber_top"))
                }
                tag(listOf(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN))
                itemTag(listOf(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN))
                dropSelf()
                dropOnState({ resin }, RubberLogBlock.HAS_RUBBER, true)
            }
            block(RUBBER_LEAVES) {
                blockState(Models.cubeTint("wood/rubber/leaves_rubber", Models.CUTOUT_RENDER_TYPE))
                tag(BlockTags.LEAVES)
                itemTag(ItemTags.LEAVES)
                dropSelfOnTool(TOOL_SHEARS)
                drop(RUBBER_SAPLING, 0.075f)
            }
            block(RUBBER_SAPLING) {
                blockState { ctx ->
                    val provider = ctx.provider()
                    provider.simpleBlock(ctx.`object`(), provider.models().cross(
                        ctx.id(), gregtech("block/wood/rubber/sapling_rubber"))
                        .renderType(Models.CUTOUT_RENDER_TYPE))
                }
                itemModel(Models.basicItem(gregtech("block/wood/rubber/sapling_rubber")))
                tag(BlockTags.SAPLINGS)
                itemTag(ItemTags.SAPLINGS)
            }
        }

        farm(RUBBER_SAPLING.get(), RUBBER_LOG.get(), RUBBER_LEAVES.get(), resin, true)

        toolCrafting {
            result("raw_rubber", "dust") {
                pattern("#")
                define('#', resin)
                toolTag(TOOL_MORTAR)
            }
        }
        extractor {
            input(resin) {
                output("raw_rubber", "dust", 3)
                voltage(Voltage.LV)
                workTicks(160)
            }
            input(RUBBER_LOG.get()) {
                output("raw_rubber", "dust")
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
        // azalea
        farm(Items.AZALEA, Items.OAK_LOG, Items.AZALEA_LEAVES, Items.OAK_WOOD)
        farm(Items.FLOWERING_AZALEA, Items.OAK_LOG, Items.FLOWERING_AZALEA_LEAVES, Items.OAK_WOOD)

        // seeding
        siftSeed(Items.FLOWERING_AZALEA, Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.CHERRY_SAPLING,
            RUBBER_SAPLING.get())
        siftSeed(Items.AZALEA_LEAVES, Items.SPRUCE_SAPLING, Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING,
            Items.DARK_OAK_SAPLING)

        // bamboo stuff
        vanilla {
            nullRecipe(Items.BAMBOO_BLOCK, Items.BAMBOO_MOSAIC, Items.BAMBOO_MOSAIC_SLAB, Items.BAMBOO_MOSAIC_STAIRS)
            nullRecipe("stick_from_bamboo")
        }
        toolCrafting {
            shapeless(Items.BAMBOO_MOSAIC, Items.BAMBOO_MOSAIC_SLAB, TOOL_SAW, 2)
        }
        assembler {
            output(Items.BAMBOO_BLOCK) {
                input(Items.BAMBOO, 8)
                voltage(Voltage.ULV)
                workTicks(64)
            }
            output(Items.BAMBOO_MOSAIC) {
                input(Items.BAMBOO_SLAB, 2)
                voltage(Voltage.ULV)
                workTicks(64)
            }
        }
        cutter {
            output(Items.BAMBOO_MOSAIC_SLAB, 2) {
                input(Items.BAMBOO_MOSAIC)
                input("water", amount = 0.1)
                voltage(Voltage.LV)
                workTicks(80)
            }
        }
        lathe {
            output(Items.BAMBOO_MOSAIC_STAIRS) {
                input(Items.BAMBOO_MOSAIC_SLAB, 2)
                voltage(Voltage.LV)
                workTicks(80)
            }
        }

        // stick
        toolCrafting {
            result(Items.STICK, 4) {
                pattern("#")
                pattern("#")
                define('#', ItemTags.PLANKS)
                toolTag(TOOL_SAW)
            }
        }
        vanilla(replace = true) {
            shaped(Items.STICK, 2) {
                pattern("#")
                pattern("#")
                define('#', ItemTags.PLANKS)
                unlockedBy("has_planks", ItemTags.PLANKS)
            }
        }
        lathe {
            defaults {
                voltage(Voltage.LV)
                workTicks(32)
            }
            output(Items.STICK, 2) {
                input(ItemTags.PLANKS)
            }
            output(Items.STICK, suffix = "_from_bamboo") {
                input(Items.BAMBOO)
            }
        }

        // to biomass
        extractor {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
            }
            input(ItemTags.LEAVES, 8) {
                output("biomass", amount = 1.6)
            }
            input(ItemTags.SAPLINGS, 16) {
                output("biomass", amount = 1.6)
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
                input("nitrogen", amount = 1)
                workTicks(320)
            }
        }
    }
}
