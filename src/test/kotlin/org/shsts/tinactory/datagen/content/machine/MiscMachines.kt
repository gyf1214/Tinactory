package org.shsts.tinactory.datagen.content.machine

import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER
import org.shsts.tinactory.content.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.AllTags.circuit
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.RegistryHelper.getBlock
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.VanillaRecipeFactory
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS
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
            result(NETWORK_CONTROLLER.get()) {
                pattern("VWV")
                pattern("VHV")
                pattern("WVW")
                define('W', ulvCable)
                define('H', ulvHull)
                define('V', ulvCircuit)
                toolTag(TOOL_WRENCH)
            }
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
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            output(NETWORK_CONTROLLER.get()) {
                component("machine_hull")
                circuit(4)
                component("cable", 2)
                input("iron", "plate", 4)
            }
            machine("steam_turbine") {
                component("machine_hull")
                circuit(2)
                component("cable", 2)
                input("iron", "rotor", 2)
                input("copper", "pipe", 2)
            }
            machine("alloy_smelter") {
                machine("electric_furnace")
                circuit(2)
                component("cable", 4)
                tech(Technologies.ALLOY_SMELTING)
            }
            machine("multiblock/interface") {
                component("machine_hull")
                circuit(2)
                component("cable", 2)
                input(Items.CHEST)
                input("glass", "primary")
                tech(Technologies.STEEL)
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

    private fun ae() {
        assembler {
            componentVoltage = Voltage.LV
            defaults {
                component("machine_hull")
                autoCable = true
                voltage(Voltage.LV)
                workTicks(MACHINE_TICKS)
            }

            output(getItem("logistics/me_signal_controller")) {
                circuit(2)
                component("sensor")
                component("emitter")
                input(Items.REDSTONE_TORCH)
                input("iron", "plate", 4)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }
            output(getItem("logistics/me_storage_detector")) {
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

            output(getItem("logistics/me_storage_interface")) {
                circuit(4)
                component("conveyor_module", 2)
                component("electric_pump", 2)
                input("stainless_steel", "plate", 4)
                tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            }
            output(getItem("logistics/me_drive")) {
                circuit(4)
                input(Items.CHEST)
                input("certus_quartz", "gem", 4)
                input("fluix", "dust", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.DIGITAL_STORAGE)
            }
        }
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
                input(ItemTags.PLANKS)
            }
            output(Items.HOPPER) {
                input(Items.CHEST)
                input("iron", "plate", 4)
            }
            output(Items.BUCKET) {
                input("iron", "plate", 2)
                input("iron", "stick")
            }
            output(Items.TORCH, 3) {
                input(Items.STICK)
                input("sulfur", "dust")
                workTicks(64)
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
            output(Items.REDSTONE_BLOCK) {
                circuit(1)
                input("redstone", "dust", 4)
                input("iron", "plate", 2)
            }
            output(Items.TRAPPED_CHEST) {
                circuit(1)
                input(Items.CHEST)
                input(Items.REDSTONE_TORCH)
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

        vanilla {
            nullRecipe(
                Items.BLAST_FURNACE,
                Items.SMOKER,
                Items.STONECUTTER,
                Items.FLETCHING_TABLE,
                Items.CARTOGRAPHY_TABLE,
                Items.GRINDSTONE,
                Items.CAMPFIRE,
                Items.SOUL_CAMPFIRE,
                Items.ENCHANTING_TABLE,
                Items.ANVIL,
                Items.SMITHING_TABLE,
                Items.CAULDRON,
                Items.BREWING_STAND,
                Items.RESPAWN_ANCHOR,
                Items.GLOWSTONE,
                Items.BUCKET,
                Items.SHEARS,
                Items.FLINT_AND_STEEL,
                Items.TNT,
                Items.SPYGLASS,
                Items.COMPASS,
                Items.CROSSBOW,
                Items.CLOCK,
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
                Items.CHEST_MINECART,
                Items.TNT_MINECART,
                Items.HOPPER_MINECART,
                Items.FURNACE_MINECART,
                Items.RAIL,
                Items.POWERED_RAIL,
                Items.DETECTOR_RAIL,
                Items.ACTIVATOR_RAIL,
                Items.LEVER,
                Items.STONE_PRESSURE_PLATE,
                Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Items.STONE_BUTTON,
                Items.POLISHED_BLACKSTONE_BUTTON,
                Items.PACKED_ICE,
                Items.BLUE_ICE,
                Items.BLAZE_POWDER,
                Items.ENDER_EYE,
                Items.END_CRYSTAL,
                Items.ENDER_CHEST)
        }
    }

    private fun misc() {
        toolCrafting {
            result(getBlock("machine/boiler/low")) {
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
            output(getBlock("machine/boiler/low")) {
                component("machine_hull")
                circuit(2)
                input(Items.FURNACE)
                component("cable", 2)
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING)
            }

            componentVoltage = Voltage.MV
            output(getBlock("machine/boiler/high")) {
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
