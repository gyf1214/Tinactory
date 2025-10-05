package org.shsts.tinactory.datagen.content.machine

import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER
import org.shsts.tinactory.content.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.AllTags.circuit
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.ToolRecipeFactory
import org.shsts.tinactory.datagen.content.builder.VanillaRecipeFactory
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS

object MiscMachines {
    private val ulvCable: Item by lazy { getComponent("cable").item(Voltage.ULV) }
    private val ulvHull: Item by lazy { getComponent("machine_hull").item(Voltage.ULV) }

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
                define('S', "stone", "primary")
                define('W', Items.STICK)
                define('C', Blocks.CRAFTING_TABLE)
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
        toolCrafting {
            ulv("stone_generator")
            ulv("ore_analyzer")
            ulv("ore_washer")
            ulv("research_bench", Blocks.CRAFTING_TABLE)
            ulv("assembler", WORKBENCH.get())
            ulv("electric_furnace", Blocks.FURNACE)
            ulv("electric_chest", Blocks.CHEST)
            ulv("electric_tank", getMaterial("glass").tag("primary"))
            ulv("logistic_worker", Blocks.HOPPER)
            ulvComponent("electric_buffer", ulvCable)

            result(NETWORK_CONTROLLER.get()) {
                pattern("VWV")
                pattern("VHV")
                pattern("WVW")
                define('W', ulvCable)
                define('H', ulvHull)
                define('V', circuit(Voltage.ULV))
                toolTag(TOOL_WRENCH)
            }
            result(getMachine("steam_turbine").block(Voltage.ULV)) {
                pattern("PVP").pattern("RHR").pattern("WVW")
                define('P', "copper", "pipe")
                define('R', "iron", "rotor")
                define('W', ulvCable)
                define('H', ulvHull)
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
            machine("alloy_smelter") {
                machine("electric_furnace")
                circuit(2)
                component("cable", 4)
                tech(Technologies.ALLOY_SMELTING)
            }
            machine("multiblock_interface") {
                component("machine_hull")
                circuit(2)
                component("cable", 2)
                input(Blocks.CHEST)
                input("glass", "primary")
                tech(Technologies.STEEL)
            }
        }
    }

    private fun ToolRecipeFactory.ulv(item: ItemLike, base: ItemLike) {
        result(item) {
            pattern("BBB").pattern("VHV").pattern("WVW")
            define('B', base.asItem())
            define('W', ulvCable)
            define('H', ulvHull)
            define('V', circuit(Voltage.ULV))
            toolTag(TOOL_WRENCH)
        }
    }

    private fun ToolRecipeFactory.ulv(item: ItemLike, base: TagKey<Item>) {
        result(item) {
            pattern("BBB").pattern("VHV").pattern("WVW")
            define('B', base)
            define('W', ulvCable)
            define('H', ulvHull)
            define('V', circuit(Voltage.ULV))
            toolTag(TOOL_WRENCH)
        }
    }

    private fun ToolRecipeFactory.ulv(name: String, base: ItemLike) {
        ulv(getMachine(name).block(Voltage.ULV), base)
    }

    private fun ToolRecipeFactory.ulv(name: String, base: TagKey<Item>) {
        ulv(getMachine(name).block(Voltage.ULV), base)
    }

    private fun ToolRecipeFactory.ulv(name: String) {
        val set = getMachine(name)
        ulv(set.block(Voltage.ULV), set.block(Voltage.PRIMITIVE))
    }

    private fun ToolRecipeFactory.ulvComponent(name: String, base: ItemLike) {
        ulv(getComponent(name).item(Voltage.ULV), base)
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
                Items.TNT,
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
                Items.POLISHED_BLACKSTONE_BUTTON,
                Items.PACKED_ICE,
                Items.BLUE_ICE)
        }
    }

    private fun misc() {
        toolCrafting {
            result(LOW_PRESSURE_BOILER.get()) {
                pattern("PPP")
                pattern("PWP")
                pattern("VFV")
                define('P', "iron", "plate")
                define('W', ulvCable)
                define('V', circuit(Voltage.ULV))
                define('F', Blocks.FURNACE.asItem())
                toolTag(TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.MV
            output(HIGH_PRESSURE_BOILER.get()) {
                component("machine_hull")
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
