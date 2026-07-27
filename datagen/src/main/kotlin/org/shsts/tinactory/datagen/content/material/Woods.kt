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
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla

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
        vanilla("crimson", nether = true, leaves = Items.NETHER_WART_BLOCK)
        vanilla("warped", nether = true)
        components("bamboo", boat = Items.BAMBOO_RAFT)

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

    private fun components(prefix: String, nether: Boolean = false, boat: ItemLike? = null) {
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
                input(TOOL_HANDLE)
            }
            output(gate) {
                input(slab, 2)
                input("redstone", "dust")
            }
            output(door) {
                input(planks, 2)
                input("redstone", "dust")
            }
            output(trapdoor) {
                input(slab, 2)
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

        if (!nether) {
            val boat1 = boat ?: vanillaItem("${prefix}_boat")
            vanilla {
                nullRecipe(boat1)
            }
            assembler {
                output(boat1) {
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

    private fun vanilla(prefix: String, nether: Boolean = false,
        sapling: ItemLike? = null, leaves: ItemLike? = null) {
        val planks = vanillaItem("${prefix}_planks")
        val logId = prefix + if (nether) "_stem" else "_log"
        val log = vanillaItem(logId)
        val logStripped = vanillaItem("stripped_$logId")
        val logsTag = AllTags.item(mcLoc("${logId}s"))
        val woodId = prefix + if (nether) "_hyphae" else "_wood"
        val wood = vanillaItem(woodId)
        val woodStripped = vanillaItem("stripped_$woodId")

        vanilla(replace = true) {
            nullRecipe(wood, woodStripped)
            shapeless(logsTag, planks, toAmount = 2, criteria = "has_logs") {
                group("planks")
            }
        }
        toolCrafting {
            shapeless(logsTag, planks, TOOL_SAW, amount = 4)
        }
        cutter {
            output(planks, 6) {
                input(logsTag)
                input("water", amount = 0.6)
                voltage(Voltage.LV)
                workTicks(240)
            }
        }
        lathe {
            defaults {
                voltage(Voltage.LV)
                workTicks(120)
            }
            output(logStripped) {
                input(log)
            }
            output(woodStripped) {
                input(wood)
            }
        }

        components(prefix, nether)

        val sapling1 = sapling ?: vanillaItem(if (nether) "${prefix}_fungus" else "${prefix}_sapling")
        val leaves1 = leaves ?: vanillaItem(if (nether) "${prefix}_wart_block" else "${prefix}_leaves")
        farm(sapling1, log, leaves1, wood)
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

        sifter {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
            }
            input(Items.FLOWERING_AZALEA_LEAVES) {
                output(Items.OAK_SAPLING, rate = 0.1)
                output(Items.BIRCH_SAPLING, rate = 0.1)
                output(RUBBER_SAPLING.get(), rate = 0.1)
            }
            input(Items.AZALEA_LEAVES) {
                output(Items.SPRUCE_SAPLING, rate = 0.1)
                output(Items.JUNGLE_SAPLING, rate = 0.1)
                output(Items.ACACIA_SAPLING, rate = 0.1)
                output(Items.DARK_OAK_SAPLING, rate = 0.1)
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
            output(Items.STICK, 2) {
                input(ItemTags.PLANKS)
                voltage(Voltage.LV)
                workTicks(32)
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
