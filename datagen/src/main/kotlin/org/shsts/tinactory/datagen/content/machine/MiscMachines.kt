package org.shsts.tinactory.datagen.content.machine

import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.hasItem
import org.shsts.tinactory.AllRecipes.hasTag
import org.shsts.tinactory.AllTags.TOOL_HAMMER
import org.shsts.tinactory.AllTags.TOOL_WRENCH
import org.shsts.tinactory.AllTags.circuit
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.laserEngraver
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactory
import org.shsts.tinactory.datagen.content.builder.VanillaRecipeFactory
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS
import org.shsts.tinactory.datagen.content.component.MiscComponents.misc
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS

object MiscMachines {
    private val ulvCable: Item by lazy { getComponent("cable").item(Voltage.ULV) }
    private val ulvHull: Item by lazy { getComponent("machine_hull").item(Voltage.ULV) }
    private val ulvCircuit: TagKey<Item> by lazy { circuit(Voltage.ULV) }

    fun init() {
        primitives()
        ulvs()
        ae()
        vanillas()
        misc()
    }

    private fun primitives() {
        vanilla {
            shaped(WORKBENCH.get()) {
                pattern("WSW")
                pattern("SCS")
                pattern("WSW")
                define('S', "stone", "primary")
                define('W', Items.STICK)
                define('C', Items.CRAFTING_TABLE)
                unlockedBy("has_cobblestone", "stone", "primary")
            }
            primitive("stone_generator") {
                pattern("WLW")
                pattern("L L")
                pattern("WLW")
                define('W', ItemTags.PLANKS)
                define('L', ItemTags.LOGS)
                unlockedBy("has_planks", ItemTags.PLANKS)
            }
            primitive("ore_analyzer") {
                pattern("WLW")
                pattern("LFL")
                pattern("WLW")
                define('W', ItemTags.PLANKS)
                define('L', ItemTags.LOGS)
                define('F', "flint", "primary")
                unlockedBy("has_flint", "flint", "primary")
            }
            primitive("ore_washer") {
                pattern("WLW")
                pattern("LFL")
                pattern("WLW")
                define('W', ItemTags.PLANKS)
                define('L', ItemTags.LOGS)
                define('F', Items.WATER_BUCKET)
                unlockedBy("has_water_bucket", Items.WATER_BUCKET)
            }
        }
    }

    private fun VanillaRecipeFactory.primitive(name: String,
        block: ShapedRecipeBuilder.() -> Unit) {
        shaped(getMachine(name).block(Voltage.PRIMITIVE), block = block)
    }

    private fun ulvs() {
        ulv("stone_generator")
        ulv("ore_analyzer")
        ulv("ore_washer")
        ulv("research_bench", Items.CRAFTING_TABLE)
        ulv("assembler", WORKBENCH.get())
        ulv("electric_furnace", Items.FURNACE)
        ulv("logistics/electric_chest", Items.CHEST)
        ulv("logistics/electric_tank", getMaterial("glass").tag("primary"))
        ulv("logistics/logistic_worker", Items.HOPPER)
        ulvComponent("electric_buffer", ulvCable)

        toolCrafting {
            result(getMachine("steam_turbine").block(Voltage.ULV)) {
                pattern("PVP").pattern("RHR").pattern("WVW")
                define('P', "copper", "pipe")
                define('R', "iron", "rotor")
                define('W', ulvCable)
                define('H', ulvHull)
                define('V', ulvCircuit)
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                component("machine_hull")
                circuit(2)
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            machine("steam_turbine") {
                component("cable", 2)
                input("iron", "rotor", 2)
                input("copper", "pipe", 2)
                tech(Technologies.SOLDERING)
            }
            machine("multiblock/interface") {
                component("cable", 2)
                input(Items.CHEST)
                input("glass", "primary")
                tech(Technologies.STEEL)
            }
            component("network_bridge") {
                component("cable", 2)
                input(Items.HOPPER, 2)
                input("glass", "primary", 2)
                tech(Technologies.SOLDERING)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            machine("alloy_smelter") {
                machine("electric_furnace")
                circuit(2)
                component("cable", 4)
                tech(Technologies.ALLOY_SMELTING)
            }
        }
    }

    private fun ulv(item: ItemLike, base: ItemLike) {
        toolCrafting {
            result(item) {
                pattern("BBB").pattern("VHV").pattern("WVW")
                define('B', base.asItem())
                define('W', ulvCable)
                define('H', ulvHull)
                define('V', ulvCircuit)
                toolTag(TOOL_WRENCH)
            }
        }
        assembler {
            output(item) {
                input(ulvHull)
                input(ulvCircuit, 2)
                input(ulvCable, 2)
                input(base, 2)
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING)
            }
        }
    }

    private fun ulv(item: ItemLike, base: TagKey<Item>) {
        toolCrafting {
            result(item) {
                pattern("BBB").pattern("VHV").pattern("WVW")
                define('B', base)
                define('W', ulvCable)
                define('H', ulvHull)
                define('V', ulvCircuit)
                toolTag(TOOL_WRENCH)
            }
        }
        assembler {
            output(item) {
                input(ulvHull)
                input(ulvCircuit, 2)
                input(ulvCable, 2)
                input(base, 2)
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING)
            }
        }
    }

    private fun ulv(name: String, base: ItemLike) {
        ulv(getMachine(name).block(Voltage.ULV), base)
    }

    private fun ulv(name: String, base: TagKey<Item>) {
        ulv(getMachine(name).block(Voltage.ULV), base)
    }

    private fun ulv(name: String) {
        val set = getMachine(name)
        ulv(set.block(Voltage.ULV), set.block(Voltage.PRIMITIVE))
    }

    private fun ulvComponent(name: String, base: ItemLike) {
        ulv(getComponent(name).item(Voltage.ULV), base)
    }

    private fun <R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>> RecipeFactory<R, B>.logistics(
        id: String, amount: Int = 1, block: B.() -> Unit) {
        output(getItem("logistics/$id"), amount, block = block)
    }

    private fun ae() {
        assembler {
            componentVoltage = Voltage.LV
            defaults {
                component("machine_hull")
                autoCable = true
                voltage(Voltage.LV)
                workTicks(MACHINE_TICKS)
            }

            logistics("me_signal_controller") {
                circuit(2)
                component("sensor")
                component("emitter")
                input(Items.REDSTONE_TORCH)
                input("iron", "plate", 4)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }
            logistics("me_storage_detector") {
                circuit(2)
                component("sensor", 2)
                input(Items.CHEST)
                input("glass", "primary")
                input("iron", "plate", 4)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }
        }
        assembler {
            componentVoltage = Voltage.HV
            defaults {
                component("machine_hull")
                autoCable = true
                voltage(Voltage.HV)
                workTicks(MACHINE_TICKS)
            }

            logistics("me_storage_interface") {
                circuit(4)
                component("conveyor_module", 2)
                component("electric_pump", 2)
                input("stainless_steel", "plate", 4)
                tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            }
            logistics("me_drive") {
                circuit(4)
                input(Items.CHEST)
                input("certus_quartz", "gem", 4)
                input("fluix", "dust", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.DIGITAL_STORAGE)
            }
            logistics("me_pattern_terminal") {
                circuit(3)
                misc("silicon_print")
                input("stainless_steel", "plate", 4)
                tech(Technologies.AUTOCRAFTING)
            }
            logistics("me_craft_terminal") {
                circuit(3)
                misc("logic_processor")
                input("titanium", "plate", 4)
                tech(Technologies.AUTOCRAFTING)
            }
            logistics("me_craft_cpu/basic") {
                circuit(2)
                misc("logic_processor", 2)
                component("sensor", 2)
                misc("storage_component/1m", 4)
                input("titanium", "plate", 4)
                tech(Technologies.AUTOCRAFTING)
            }
        }
        assembler {
            componentVoltage = Voltage.IV
            defaults {
                component("machine_hull")
                autoCable = true
                voltage(Voltage.IV)
                workTicks(MACHINE_TICKS)
            }
            logistics("me_craft_cpu/advanced") {
                circuit(2)
                misc("calculation_processor", 2)
                component("sensor", 2)
                misc("storage_component/16m", 2)
                input("tungsten_steel", "plate", 4)
                tech(Technologies.AUTOCRAFTING)
            }
        }
    }

    private fun template(output: ItemLike, lens: String, mat: String? = null, base: ItemLike? = null,
        disable: Boolean = true) {
        if (disable) {
            vanilla {
                nullRecipe(output)
            }
        }
        laserEngraver {
            output(output) {
                if (mat != null) {
                    input(mat, "plate")
                } else if (base != null) {
                    input(base)
                }
                input(lens, "lens", 0, port = 1)
                voltage(Voltage.MV)
                workTicks(256)
                requireCleanness(-0.5, 0.5)
            }
        }
    }

    private fun trim(output: String, mat: String, lens: String) {
        template(vanillaItem("${output}_armor_trim_smithing_template"), lens, mat = mat)
    }

    private fun pattern(output: String, lens: String, disable: Boolean) {
        template(vanillaItem("${output}_banner_pattern"), lens, base = Items.PAPER, disable = disable)
    }

    private fun vanillas() {
        toolCrafting {
            result(Items.HOPPER) {
                pattern("P P")
                pattern("PCP")
                pattern(" P ")
                define('P', "iron", "plate")
                define('C', Items.CHEST)
                toolTag(TOOL_WRENCH, TOOL_HAMMER)
            }
            result(Items.BUCKET) {
                pattern("P P")
                pattern(" P ")
                define('P', "iron", "plate")
                toolTag(TOOL_HAMMER)
            }
            result(Items.LEVER) {
                pattern("P")
                pattern("S")
                pattern("R")
                define('P', "iron", "plate")
                define('S', Items.STICK)
                define('R', "redstone", "dust")
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.ULV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.SOLDERING)
            }
            output(Items.FURNACE) {
                input("stone", "primary", 8)
            }
            output(Items.CHEST) {
                input(ItemTags.PLANKS, 8)
            }
            output(Items.BARREL) {
                input(ItemTags.PLANKS, 6)
                input(ItemTags.WOODEN_SLABS, 2)
            }
            output(Items.HOPPER) {
                input(Items.CHEST)
                input("iron", "plate", 4)
            }
            output(Items.BUCKET) {
                input("iron", "plate", 2)
                input("iron", "stick")
            }
            output(Items.RAIL, 16) {
                input(ItemTags.WOODEN_SLABS)
                input("iron", "stick", 6)
            }
            output(Items.POWERED_RAIL, 8) {
                input(Items.RAIL, 8)
                input("gold", "bolt", 6)
                input("redstone", "dust")
            }
            output(Items.DETECTOR_RAIL, 8) {
                input(Items.RAIL, 8)
                input(Items.STONE_PRESSURE_PLATE)
                input("redstone", "dust")
            }
            output(Items.ACTIVATOR_RAIL, 8) {
                input(Items.RAIL, 8)
                input("iron", "ring", 6)
                input(Items.REDSTONE_TORCH)
            }
            output(Items.MINECART) {
                input("iron", "plate", 2)
                input("iron", "stick", 2)
                input("iron", "ring", 4)
            }
            output(Items.TORCH, 6) {
                input(Items.STICK)
                input("sulfur", "dust")
                workTicks(64)
            }
            output(Items.SOUL_TORCH, 6) {
                input(Items.STICK)
                input("sulfur", "dust")
                input(ItemTags.SOUL_FIRE_BASE_BLOCKS)
                workTicks(64)
            }
            output(Items.GLOWSTONE) {
                input(Items.GLOWSTONE_DUST, 4)
            }
            output(Items.ARMOR_STAND) {
                input(Items.STICK, 6)
                input(Items.SMOOTH_STONE_SLAB)
            }
            output(Items.CANDLE) {
                input(Items.STRING)
                input(Items.HONEYCOMB)
            }
            output(Items.LANTERN) {
                input(Items.TORCH)
                input("iron", "ring", 2)
                input("iron", "screw", 2)
            }
            output(Items.SOUL_LANTERN) {
                input(Items.SOUL_TORCH)
                input("iron", "ring", 2)
                input("iron", "screw", 2)
            }
            output(Items.REDSTONE_TORCH) {
                input(Items.STICK)
                input("redstone", "dust")
                workTicks(64)
            }
            output(Items.LEVER) {
                input("iron", "plate")
                input(Items.STICK)
                input("redstone", "dust")
            }
            output(Items.TRIPWIRE_HOOK, 2) {
                input("iron", "plate", 2)
                input(Items.STICK, 2)
                input(Items.REDSTONE_TORCH)
            }
            output(Items.PAINTING) {
                input(ItemTags.WOOL)
                input(Items.STICK, 8)
            }
            output(Items.ITEM_FRAME) {
                input(Items.LEATHER)
                input(Items.STICK, 8)
            }
            output(Items.LEAD) {
                input(Items.STRING, 2)
                input("rubber", amount = 0.5)
            }
            output(Items.LADDER) {
                input(Items.STICK, 2)
                input("iron", "bolt")
            }
            output(Items.LOOM) {
                input(ItemTags.PLANKS, 2)
                input(Items.STRING, 2)
                input("iron", "stick")
                input("redstone", "dust")
            }
            output(Items.SCAFFOLDING, 6) {
                input(Items.STICK, 6)
                input(Items.STRING)
            }
            output(Items.BOOK) {
                input(Items.PAPER, 3)
                input(Items.LEATHER)
                input(Items.STRING)
            }
            output(Items.BOOKSHELF) {
                input(ItemTags.PLANKS, 6)
                input(Items.BOOK, 3)
            }
            output(Items.CHISELED_BOOKSHELF) {
                input(ItemTags.PLANKS, 6)
                input(ItemTags.WOODEN_SLABS, 3)
            }
            output(Items.LECTERN) {
                input(Items.BOOKSHELF)
                input(ItemTags.WOODEN_SLABS, 4)
            }
            output(Items.FLOWER_POT) {
                input("iron", "ring")
                input(Items.BRICK, 2)
            }
            output(Items.GLASS_BOTTLE) {
                input(Items.GLASS_PANE, 3)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.LV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }

            componentVoltage = Voltage.ULV
            output(Items.REPEATER) {
                circuit(1)
                input("redstone", "dust")
                input("iron", "plate", 2)
            }
            output(Items.COMPARATOR) {
                circuit(1)
                input(Items.REDSTONE_TORCH, 4)
                input("iron", "plate", 2)
            }
            output(Items.REDSTONE_LAMP) {
                circuit(1)
                input("glowstone", "dust", 2)
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.COPPER_BULB) {
                circuit(1)
                input("redstone", "dust", 3)
                input("copper", "plate", 3)
            }
            output(Items.REDSTONE_BLOCK) {
                circuit(1)
                input("redstone", "dust", 9)
            }
            output(Items.TRAPPED_CHEST) {
                circuit(1)
                input(Items.CHEST)
                input(Items.REDSTONE_TORCH)
            }
            output(Items.SEA_LANTERN) {
                circuit(1)
                input(Items.PRISMARINE_CRYSTALS, 4)
                input(Items.PRISMARINE_SHARD, 4)
            }

            componentVoltage = Voltage.LV
            output(Items.OBSERVER) {
                component("sensor")
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.DAYLIGHT_DETECTOR) {
                component("sensor")
                input("redstone", "dust", 2)
                input("glass", "primary", 2)
            }
            output(Items.TARGET) {
                component("sensor")
                input("redstone", "dust", 2)
                input(Items.WHEAT, 9)
            }
            output(Items.PISTON) {
                component("electric_piston")
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.STICKY_PISTON) {
                component("electric_piston")
                input("redstone", "dust", 2)
                input("rubber", amount = 2)
            }
            output(Items.DROPPER) {
                component("electric_pump")
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.DISPENSER) {
                component("conveyor_module")
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.NOTE_BLOCK) {
                component("emitter")
                input("redstone", "dust", 2)
                input("iron", "plate", 2)
            }
            output(Items.JUKEBOX) {
                component("emitter")
                input("diamond", "gem")
                input("steel", "plate", 2)
            }
        }

        vanilla(replace = true) {
            shapeless(Items.WRITABLE_BOOK) {
                requires(Items.BOOK)
                requires(Items.BLACK_DYE)
                requires(Items.FEATHER)
                unlockedBy("has_book", hasItem(Items.BOOK))
            }

            shapeless(Items.FLINT_AND_STEEL, category = RecipeCategory.TOOLS) {
                val steel = getMaterial("steel").tag("ingot")
                requires(steel)
                requires(Items.FLINT)
                unlockedBy("has_steel", hasTag(steel))
            }

            nullRecipe(
                Items.BLAST_FURNACE,
                Items.STONECUTTER,
                Items.COMPOSTER,
                Items.CRAFTER,
                Items.CHISELED_BOOKSHELF,
                Items.GLOWSTONE,
                Items.SHULKER_BOX,
                "shulker_box_coloring",
                Items.ARMOR_STAND,
                Items.CANDLE,
                Items.LANTERN,
                Items.SOUL_LANTERN,
                Items.BUCKET,
                Items.TNT,
                Items.REDSTONE_TORCH,
                Items.TARGET,
                Items.NOTE_BLOCK,
                Items.JUKEBOX,
                Items.PISTON,
                Items.STICKY_PISTON,
                Items.DISPENSER,
                Items.DROPPER,
                Items.DAYLIGHT_DETECTOR,
                Items.TRIPWIRE_HOOK,
                Items.TRAPPED_CHEST,
                Items.REPEATER,
                Items.COMPARATOR,
                Items.REDSTONE_LAMP,
                Items.OBSERVER,
                Items.HOPPER,
                Items.MINECART,
                Items.RAIL,
                Items.POWERED_RAIL,
                Items.DETECTOR_RAIL,
                Items.ACTIVATOR_RAIL,
                Items.LEVER,
                Items.PACKED_ICE,
                Items.BLUE_ICE,
                Items.BLAZE_POWDER,
                Items.ENDER_EYE,
                Items.END_CRYSTAL,
                Items.ENDER_CHEST,
                Items.PAINTING,
                Items.ITEM_FRAME,
                Items.LEAD,
                Items.LOOM,
                Items.BOOK,
                Items.BOOKSHELF,
                Items.LECTERN,
                Items.FLOWER_POT,
                Items.GLASS_BOTTLE,
                Items.SEA_LANTERN)
        }

        template(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "ender_eye", mat = "stainless_steel")

        trim("sentry", "steel", "ruby")
        trim("dune", "steel", "diamond")
        trim("coast", "steel", "sapphire")
        trim("wild", "steel", "emerald")
        trim("ward", "steel", "topaz")
        trim("eye", "steel", "blue_topaz")
        trim("vex", "annealed_copper", "ruby")
        trim("tide", "annealed_copper", "diamond")
        trim("snout", "annealed_copper", "sapphire")
        trim("rib", "annealed_copper", "emerald")
        trim("spire", "annealed_copper", "topaz")
        trim("wayfinder", "annealed_copper", "blue_topaz")
        trim("shaper", "tungsten_steel", "ruby")
        trim("silence", "tungsten_steel", "diamond")
        trim("raiser", "tungsten_steel", "sapphire")
        trim("host", "tungsten_steel", "emerald")
        trim("flow", "tungsten_steel", "topaz")
        trim("bolt", "tungsten_steel", "blue_topaz")

        pattern("flower", "ruby", true)
        pattern("globe", "diamond", false)
        pattern("creeper", "sapphire", true)
        pattern("skull", "emerald", true)
        pattern("piglin", "topaz", false)
        pattern("flow", "blue_topaz", false)
        pattern("guster", "ender_eye", false)
        pattern("mojang", "nether_star", true)
    }

    private fun misc() {
        toolCrafting {
            result(getItem("machine/boiler/low")) {
                pattern("PPP")
                pattern("PWP")
                pattern("VFV")
                define('P', "iron", "plate")
                define('W', ulvCable)
                define('V', ulvCircuit)
                define('F', Items.FURNACE)
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            output(getItem("machine/boiler/low")) {
                component("machine_hull")
                circuit(2)
                input(Items.FURNACE)
                component("cable", 2)
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING)
            }

            componentVoltage = Voltage.MV
            output(getItem("machine/boiler/high")) {
                component("machine_hull")
                input(Items.FURNACE)
                input("brass", "pipe", 2)
                input("iron", "plate", 4)
                voltage(Voltage.LV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING, Technologies.STEEL)
            }
        }
    }
}
