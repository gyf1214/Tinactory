package org.shsts.tinactory.datagen.content.machine

import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER
import org.shsts.tinactory.content.AllBlockEntities.ARC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.ASSEMBLER
import org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX
import org.shsts.tinactory.content.AllBlockEntities.BENDER
import org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE
import org.shsts.tinactory.content.AllBlockEntities.CHEMICAL_REACTOR
import org.shsts.tinactory.content.AllBlockEntities.CIRCUIT_ASSEMBLER
import org.shsts.tinactory.content.AllBlockEntities.COMBUSTION_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.CUTTER
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_TANK
import org.shsts.tinactory.content.AllBlockEntities.ELECTROLYZER
import org.shsts.tinactory.content.AllBlockEntities.EXTRACTOR
import org.shsts.tinactory.content.AllBlockEntities.EXTRUDER
import org.shsts.tinactory.content.AllBlockEntities.FLUID_SOLIDIFIER
import org.shsts.tinactory.content.AllBlockEntities.GAS_TURBINE
import org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.LASER_ENGRAVER
import org.shsts.tinactory.content.AllBlockEntities.LATHE
import org.shsts.tinactory.content.AllBlockEntities.LOGISTIC_WORKER
import org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER
import org.shsts.tinactory.content.AllBlockEntities.MACERATOR
import org.shsts.tinactory.content.AllBlockEntities.ME_DRIVER
import org.shsts.tinactory.content.AllBlockEntities.ME_STORAGE_INTERFACE
import org.shsts.tinactory.content.AllBlockEntities.MIXER
import org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER
import org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER
import org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER
import org.shsts.tinactory.content.AllBlockEntities.POLARIZER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_ANALYZER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_WASHER
import org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_STONE_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.RESEARCH_BENCH
import org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE
import org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.THERMAL_CENTRIFUGE
import org.shsts.tinactory.content.AllBlockEntities.WIREMILL
import org.shsts.tinactory.content.AllBlockEntities.WORKBENCH
import org.shsts.tinactory.content.AllItems.ELECTRIC_BUFFER
import org.shsts.tinactory.content.AllItems.TRANSFORMER
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.CLEANROOM_CONNECTOR
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.machine.MachineSet
import org.shsts.tinactory.content.machine.ProcessingSet
import org.shsts.tinactory.datagen.content.Models.cubeBlock
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.model.MachineModel
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_OUT_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS
import org.shsts.tinycorelib.api.registrate.entry.IEntry
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
        primitiveMachine(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR, "machines/rock_crusher")
        primitiveMachine(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER, "machines/electromagnetic_separator")
        primitiveMachine(ORE_WASHER, PRIMITIVE_ORE_WASHER, "machines/ore_washer")
        machine(RESEARCH_BENCH, "overlay/machine/overlay_screen")
        machine(ASSEMBLER)
        machine(LASER_ENGRAVER)
        machine(CIRCUIT_ASSEMBLER, "machines/assembler")
        machine(MACERATOR)
        machine(CENTRIFUGE)
        machine(THERMAL_CENTRIFUGE)
        machine(ELECTRIC_FURNACE, "machines/electric_furnace")
        machine(ALLOY_SMELTER)
        machine(MIXER)
        machine(POLARIZER)
        machine(WIREMILL)
        machine(BENDER)
        machine(LATHE)
        machine(CUTTER)
        machine(EXTRUDER)
        machine(EXTRACTOR)
        machine(FLUID_SOLIDIFIER)
        machine(ELECTROLYZER)
        machine(CHEMICAL_REACTOR)
        machine(ARC_FURNACE)
        machine(STEAM_TURBINE) {
            overlay(Direction.NORTH, TURBINE_SIDE)
            overlay(Direction.SOUTH, TURBINE_SIDE)
        }
        machine(GAS_TURBINE) {
            overlay(Direction.EAST, TURBINE_SIDE)
            overlay(Direction.WEST, TURBINE_SIDE)
        }
        machine(COMBUSTION_GENERATOR, "generators/combustion")
    }

    private fun logistics() {
        machine(ELECTRIC_CHEST) {
            overlay(Direction.UP, "overlay/machine/overlay_qchest")
            overlay(Direction.NORTH, SCREEN_GLASS)
        }
        machine(ELECTRIC_TANK) {
            overlay(Direction.UP, "overlay/machine/overlay_qtank")
            overlay(Direction.NORTH, SCREEN_GLASS)
        }
        machine(LOGISTIC_WORKER, "cover/overlay_conveyor")
        machine(ME_DRIVER, "overlay/automation/automation_superbuffer")
        machine(ME_STORAGE_INTERFACE, "cover/overlay_storage")
    }

    private fun misc() {
        machine(BATTERY_BOX, IO_OUT_TEX)

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
            for (entry in TRANSFORMER.values + ELECTRIC_BUFFER.values) {
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
        is ProcessingSet -> AllTags.machineTag(set.recipeType)
        ELECTRIC_FURNACE -> AllTags.ELECTRIC_FURNACE
        else -> null
    }

    fun IBlockDataBuilder<out Block, *>.machineModel(block: MachineModel.Builder<*>.() -> Unit) {
        MachineModel.builder(this).apply {
            block()
            build()
        }
    }

    private fun machine(set: MachineSet, block: MachineModel.Builder<*>.() -> Unit) {
        val tag = machineTag(set)
        tag?.let { dataGen { tag(it, AllTags.MACHINE) } }
        blockData {
            for (v in set.voltages) {
                block(set.entry(v)) {
                    machineModel(block)
                    tag(MINEABLE_WITH_WRENCH)
                    tag?.let { itemTag(it) }
                }
            }
        }
    }

    private fun machine(set: MachineSet, overlay: String) {
        machine(set) { overlay(overlay) }
    }

    private fun machine(set: ProcessingSet) {
        machine(set, "machines/${set.recipeType.id()}")
    }

    private fun primitiveMachine(set: MachineSet, primitive: IEntry<out Block>,
        overlay: String) {
        machine(set, overlay)
        val tag = machineTag(set)
        blockData(primitive) {
            machineModel { overlay(overlay) }
            tag(MINEABLE_WITH_WRENCH)
            tag(BlockTags.MINEABLE_WITH_AXE)
            tag?.let { itemTag(tag) }
        }
    }
}
