package org.shsts.tinactory.datagen.content.machine

import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER
import org.shsts.tinactory.content.AllBlockEntities.ASSEMBLER
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_TANK
import org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.LOGISTIC_WORKER
import org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.MULTIBLOCK_INTERFACE
import org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER
import org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER
import org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_ANALYZER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_WASHER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_STONE_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.RESEARCH_BENCH
import org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE
import org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.ELECTRIC_BUFFER
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllMultiblocks.BLAST_FURNACE
import org.shsts.tinactory.content.AllMultiblocks.HEATPROOF_CASING
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.AllTags.circuit
import org.shsts.tinactory.content.machine.MachineSet
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.ToolRecipeFactory
import org.shsts.tinactory.datagen.content.component.Component
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS
import org.shsts.tinycorelib.api.registrate.entry.IEntry

object MiscMachines {
    fun init() {
        primitives()
        ulvs()
        vanillas()
        misc()
    }

    private fun primitives() {
        vanilla {
            shaped(WORKBENCH.get()) {
                pattern("WSW")
                pattern("SCS")
                pattern("WSW")
                define('S', "stone", "block")
                define('W', Items.STICK)
                define('C', Blocks.CRAFTING_TABLE)
                unlockedBy("has_cobblestone", "stone", "block")
            }
            shaped(PRIMITIVE_STONE_GENERATOR.get()) {
                pattern("WLW")
                pattern("L L")
                pattern("WLW")
                define('W', ItemTags.PLANKS)
                define('L', ItemTags.LOGS)
                unlockedBy("has_planks", ItemTags.PLANKS)
            }
            shaped(PRIMITIVE_ORE_ANALYZER.get()) {
                pattern("WLW")
                pattern("LFL")
                pattern("WLW")
                define('W', ItemTags.PLANKS)
                define('L', ItemTags.LOGS)
                define('F', "flint", "primary")
                unlockedBy("has_flint", "flint", "primary")
            }
            shaped(PRIMITIVE_ORE_WASHER.get()) {
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

    private fun ulvs() {
        toolCrafting {
            ulv(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR)
            ulv(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER)
            ulv(ORE_WASHER, PRIMITIVE_ORE_WASHER)
            ulv(RESEARCH_BENCH, Blocks.CRAFTING_TABLE)
            ulv(ASSEMBLER, WORKBENCH)
            ulv(ELECTRIC_FURNACE, Blocks.FURNACE)
            ulv(ELECTRIC_CHEST, Blocks.CHEST)
            ulv(ELECTRIC_TANK, "glass", "primary")
            ulv(LOGISTIC_WORKER, Blocks.HOPPER)
            ulv(ELECTRIC_BUFFER, CABLE.item(Voltage.ULV))

            result(NETWORK_CONTROLLER.get()) {
                pattern("VWV")
                pattern("VHV")
                pattern("WVW")
                define('W', CABLE.item(Voltage.ULV))
                define('H', MACHINE_HULL.item(Voltage.ULV))
                define('V', circuit(Voltage.ULV))
                toolTag(TOOL_WRENCH)
            }
            result(STEAM_TURBINE.block(Voltage.ULV)) {
                pattern("PVP").pattern("RHR").pattern("WVW")
                define('P', "copper", "pipe")
                define('R', "iron", "rotor")
                define('W', CABLE.item(Voltage.ULV))
                define('H', MACHINE_HULL.item(Voltage.ULV))
                define('V', circuit(Voltage.ULV))
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            output(ALLOY_SMELTER) {
                input(ELECTRIC_FURNACE)
                circuit(2)
                input(CABLE, 4)
                tech(Technologies.ALLOY_SMELTING)
            }
            output(BLAST_FURNACE.get()) {
                input(HEATPROOF_CASING.get())
                input(ELECTRIC_FURNACE, 3)
                circuit(3)
                input(CABLE, 2)
                tech(Technologies.STEEL)
            }
            output(MULTIBLOCK_INTERFACE) {
                input(MACHINE_HULL)
                circuit(2)
                input(CABLE, 2)
                input(Blocks.CHEST)
                input("glass", "primary")
                tech(Technologies.STEEL)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun ToolRecipeFactory.ulv(item: ItemLike, base: Any? = null, sub: String? = null) {
        result(item) {
            pattern("BBB").pattern("VHV").pattern("WVW")
            when (base) {
                is ItemLike -> define('B', base.asItem())
                is TagKey<*> -> define('B', base as TagKey<Item>)
                is String -> define('B', base, sub!!)
            }
            define('W', CABLE.item(Voltage.ULV))
            define('H', MACHINE_HULL.item(Voltage.ULV))
            define('V', circuit(Voltage.ULV))
            toolTag(TOOL_WRENCH)
        }
    }

    private fun ToolRecipeFactory.ulv(machine: MachineSet, base: IEntry<out ItemLike>) {
        ulv(machine.block(Voltage.ULV), base.get())
    }

    private fun ToolRecipeFactory.ulv(machine: MachineSet, base: ItemLike) {
        ulv(machine.block(Voltage.ULV), base)
    }

    private fun ToolRecipeFactory.ulv(machine: MachineSet, name: String, sub: String) {
        ulv(machine.block(Voltage.ULV), name, sub)
    }

    private fun ToolRecipeFactory.ulv(machine: Component, base: ItemLike) {
        ulv(machine.item(Voltage.ULV), base)
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
        }

        vanilla {
            nullRecipe(Items.BLAST_FURNACE,
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
                Items.TARGET,
                Items.NOTE_BLOCK,
                Items.JUKEBOX,
                Items.CAULDRON,
                Items.RESPAWN_ANCHOR,
                Items.GLOWSTONE,
                Items.BUCKET,
                Items.SHEARS,
                Items.FLINT_AND_STEEL,
                Items.SPYGLASS,
                Items.COMPASS,
                Items.CROSSBOW,
                Items.CLOCK,
                Items.PISTON,
                Items.STICKY_PISTON,
                Items.DISPENSER,
                Items.DROPPER,
                Items.DAYLIGHT_DETECTOR,
                Items.TRIPWIRE_HOOK,
                Items.TRAPPED_CHEST,
                Items.HOPPER,
                Items.REDSTONE_TORCH,
                Items.REPEATER,
                Items.COMPARATOR,
                Items.REDSTONE_LAMP,
                Items.OBSERVER,
                Items.MINECART,
                Items.CHEST_MINECART,
                Items.TNT_MINECART,
                Items.HOPPER_MINECART,
                Items.FURNACE_MINECART,
                Items.RAIL,
                Items.POWERED_RAIL,
                Items.DETECTOR_RAIL,
                Items.ACTIVATOR_RAIL,
                Items.STONE_PRESSURE_PLATE,
                Items.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                Items.LIGHT_WEIGHTED_PRESSURE_PLATE,
                Items.HEAVY_WEIGHTED_PRESSURE_PLATE,
                Items.STONE_BUTTON,
                Items.POLISHED_BLACKSTONE_BUTTON)

        }
    }

    private fun misc() {
        toolCrafting {
            result(LOW_PRESSURE_BOILER.get()) {
                pattern("PPP")
                pattern("PWP")
                pattern("VFV")
                define('P', "iron", "plate")
                define('W', CABLE.item(Voltage.ULV))
                define('V', circuit(Voltage.ULV))
                define('F', Blocks.FURNACE.asItem())
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.MV
            output(HIGH_PRESSURE_BOILER.get()) {
                input(MACHINE_HULL)
                input(Blocks.FURNACE)
                input("brass", "pipe", 2)
                input("iron", "plate", 4)
                voltage(Voltage.LV)
                workTicks(MACHINE_TICKS)
                tech(Technologies.SOLDERING, Technologies.STEEL)
            }
        }
    }
}
