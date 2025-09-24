package org.shsts.tinactory.datagen.content.machine

import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER
import org.shsts.tinactory.content.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllItems.getComponentEntry
import org.shsts.tinactory.content.AllRecipes
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.CLEANROOM_CONNECTOR
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.content.machine.MachineSet
import org.shsts.tinactory.content.machine.ProcessingSet
import org.shsts.tinactory.content.network.SubnetBlock
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Models.cubeBlock
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.model.MachineModel
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_OUT_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder

object Machines {
    private const val TURBINE_SIDE = "generators/steam_turbine/overlay_side"
    private const val SCREEN_GLASS = "overlay/machine/overlay_screen_glass"
    private const val BOILER_TEX = "generators/boiler/coal"
    const val MACHINE_TICKS = 200L

    fun init() {
        processings()
        logistics()
        misc()
    }

    private fun processings() {
        primitiveMachine("stone_generator", "machines/rock_crusher")
        primitiveMachine("ore_analyzer", "machines/electromagnetic_separator")
        primitiveMachine("ore_washer", "machines/ore_washer")
        machine("research_bench", "overlay/machine/overlay_screen")
        machine("assembler")
        machine("laser_engraver")
        machine("circuit_assembler", "machines/assembler")
        machine("macerator")
        machine("centrifuge")
        machine("thermal_centrifuge")
        machine("electric_furnace", "machines/electric_furnace")
        machine("alloy_smelter")
        machine("mixer")
        machine("polarizer")
        machine("wiremill")
        machine("bender")
        machine("lathe")
        machine("cutter")
        machine("extruder")
        machine("extractor")
        machine("fluid_solidifier")
        machine("electrolyzer")
        machine("chemical_reactor")
        machine("arc_furnace")
        machine("steam_turbine") {
            overlay(Direction.NORTH, TURBINE_SIDE)
            overlay(Direction.SOUTH, TURBINE_SIDE)
        }
        machine("gas_turbine") {
            overlay(Direction.EAST, TURBINE_SIDE)
            overlay(Direction.WEST, TURBINE_SIDE)
        }
        machine("combustion_generator", "generators/combustion")

        dataGen {
            for (type in AllRecipes.PROCESSING_TYPES) {
                val tag = AllTags.machine(type.recipeType)
                tag(tag, AllTags.MACHINE)
            }
            tag(AllTags.ELECTRIC_FURNACE, AllTags.MACHINE)
        }
    }

    private fun logistics() {
        machine("electric_chest") {
            overlay(Direction.UP, "overlay/machine/overlay_qchest")
            overlay(Direction.NORTH, SCREEN_GLASS)
        }
        machine("electric_tank") {
            overlay(Direction.UP, "overlay/machine/overlay_qtank")
            overlay(Direction.NORTH, SCREEN_GLASS)
        }
        machine("logistic_worker", "cover/overlay_conveyor")
        machine("me_drive", "overlay/automation/automation_superbuffer")
        machine("me_storage_interface", "cover/overlay_storage")
    }

    private fun misc() {
        machine("battery_box", IO_OUT_TEX)

        blockData {
            defaults {
                tag(MINEABLE_WITH_WRENCH)
            }
            block(NETWORK_CONTROLLER) {
                machineModel {
                    casing("casings/computer/computer_casing")
                    overlay("overlay/machine/overlay_maintenance_full_auto")
                    ioTex(ME_BUS)
                }
            }
            block(WORKBENCH) {
                blockState(cubeBlock("casings/crafting_table"))
                tag(BlockTags.MINEABLE_WITH_AXE)
            }
            block(LOW_PRESSURE_BOILER) {
                machineModel {
                    casing(Voltage.ULV)
                    overlay(BOILER_TEX)
                    ioTex(ME_BUS)
                }
            }
            block(HIGH_PRESSURE_BOILER) {
                machineModel {
                    casing(Voltage.MV)
                    overlay(BOILER_TEX)
                    ioTex(ME_BUS)
                }
            }

            val subnets = listOf("transformer", "electric_buffer")
                .flatMap { getComponentEntry<SubnetBlock>(it).values }
            for (entry in subnets) {
                block(entry) {
                    machineModel {
                        overlay(Direction.NORTH, IO_TEX)
                        overlay(Direction.SOUTH, IO_OUT_TEX)
                    }
                    tag(CLEANROOM_CONNECTOR)
                }
            }
        }
    }

    private fun machineTag(set: MachineSet) = when (set) {
        is ProcessingSet -> AllTags.machine(set.recipeType)
        ELECTRIC_FURNACE -> AllTags.ELECTRIC_FURNACE
        else -> null
    }

    fun IBlockDataBuilder<out Block, *>.machineModel(block: MachineModel.Builder<*>.() -> Unit) {
        MachineModel.builder(this).apply {
            block()
            build()
        }
    }

    private fun machine(name: String, block: MachineModel.Builder<*>.() -> Unit) {
        val set = getMachine(name)
        val tag = machineTag(set)
        blockData {
            for (v in set.voltages.filter { it != Voltage.PRIMITIVE }) {
                block(set.entry(v)) {
                    machineModel(block)
                    tag(MINEABLE_WITH_WRENCH)
                    tag?.let { itemTag(it) }
                }
            }
        }
    }

    private fun machine(name: String, overlay: String) {
        machine(name) { overlay(overlay) }
    }

    private fun machine(name: String) {
        machine(name, "machines/$name")
    }

    private fun primitiveMachine(name: String, overlay: String) {
        machine(name, overlay)
        val set = getMachine(name)
        val tag = machineTag(set)
        blockData(set.entry(Voltage.PRIMITIVE)) {
            machineModel { overlay(overlay) }
            tag(MINEABLE_WITH_WRENCH)
            tag(BlockTags.MINEABLE_WITH_AXE)
            tag?.let { itemTag(tag) }
        }
    }
}
