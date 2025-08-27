package org.shsts.tinactory.datagen.content.component

import org.shsts.tinactory.content.AllItems.ADVANCED_GRINDER
import org.shsts.tinactory.content.AllItems.BATTERY
import org.shsts.tinactory.content.AllItems.BOULES
import org.shsts.tinactory.content.AllItems.BUZZSAW
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.CHIPS
import org.shsts.tinactory.content.AllItems.COMPONENT_ITEMS
import org.shsts.tinactory.content.AllItems.FERTILIZER
import org.shsts.tinactory.content.AllItems.FLUID_CELL
import org.shsts.tinactory.content.AllItems.FLUID_STORAGE_CELL
import org.shsts.tinactory.content.AllItems.GOOD_GRINDER
import org.shsts.tinactory.content.AllItems.ITEM_FILTER
import org.shsts.tinactory.content.AllItems.ITEM_STORAGE_CELL
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllItems.RAW_WAFERS
import org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT
import org.shsts.tinactory.content.AllItems.WAFERS
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.CircuitTier.CRYSTAL
import org.shsts.tinactory.content.electric.CircuitTier.NANO
import org.shsts.tinactory.content.electric.CircuitTier.QUANTUM
import org.shsts.tinactory.content.electric.Circuits
import org.shsts.tinactory.content.electric.Circuits.board
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.content.electric.Voltage.ULV
import org.shsts.tinactory.core.util.LocHelper.ae2
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.machineItem
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.itemData
import org.shsts.tinactory.datagen.content.builder.ItemDataFactory
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX

object Components {
    private const val RESEARCH_TEX = "metaitems/glass_vial/"
    private const val GRINDER_TEX = "metaitems/component.grinder"
    const val COMPONENT_TICKS = 100L

    fun init() {
        components()
        chips()
        circuits()
        tools()
    }

    private fun components() {
        for (entry in CABLE.values) {
            blockData(entry) {
                blockState(Models::cableBlock)
                itemModel(Models::cableItem)
                tag(MINEABLE_WITH_CUTTER)
            }
        }

        itemData {
            for (entry in COMPONENT_ITEMS) {
                item(entry) { model(Models::componentItem) }
            }

            for ((v, entry) in BATTERY) {
                item(entry) {
                    model(Models::batteryItem)
                    tag(AllTags.battery(v))
                }
            }

            for ((v, entry) in MACHINE_HULL) {
                item(entry) { model(machineItem(v, IO_TEX)) }
            }

            for (entry in RESEARCH_EQUIPMENT.values) {
                item(entry) { model(basicItem("${RESEARCH_TEX}base", "${RESEARCH_TEX}overlay")) }
            }

            item(GOOD_GRINDER) { model(basicItem("$GRINDER_TEX.diamond")) }
            item(ADVANCED_GRINDER) { model(basicItem("$GRINDER_TEX.tungsten")) }

            for (entry in BUZZSAW.values) {
                item(entry) { model(basicItem("tools/buzzsaw")) }
            }

            for (entry in listOf(ITEM_FILTER, FERTILIZER)) {
                item(entry) { model(Models::simpleItem) }
            }

            for ((i, entry) in ITEM_STORAGE_CELL.withIndex()) {
                val k = 1 shl (2 * i)
                item(entry) {
                    model(basicItem(ae2("item/item_storage_cell_${k}k")))
                    tag(AllTags.ITEM_STORAGE_CELL)
                }
                item(FLUID_STORAGE_CELL[i]) {
                    model(basicItem(ae2("item/fluid_storage_cell_${k}k")))
                    tag(AllTags.FLUID_STORAGE_CELL)
                }
            }
        }
    }

    private fun chips() {
        itemData {
            for (entry in BOULES + RAW_WAFERS) {
                val name = entry.id()
                    .replace('/', '.')
                    .replace("wafer_raw.", "wafer.")
                    .replace("glowstone", "phosphorus")
                item(entry) { model(basicItem("metaitems/$name")) }
            }


            chip("integrated_circuit", "integrated_logic_circuit")
            chip("cpu", "central_processing_unit")
            chip("nano_cpu", "nano_central_processing_unit")
            chip("qbit_cpu", "qbit_central_processing_unit")
            chip("ram", "random_access_memory")
            chip("nand", "nand_memory_chip")
            chip("nor", "nor_memory_chip")
            chip("simple_soc", "simple_system_on_chip")
            chip("soc", "system_on_chip")
            chip("advanced_soc", "advanced_system_on_chip")
            chip("low_pic", "low_power_integrated_circuit")
            chip("pic", "power_integrated_circuit")
            chip("high_pic", "high_power_integrated_circuit")
        }
    }

    private fun ItemDataFactory.chip(name: String, tex: String) {
        for (entry in listOf(WAFERS.getValue(name), CHIPS.getValue(name))) {
            val texName = entry.id()
                .replace('/', '.')
                .replace("chip.", "plate.")
                .replace(name, tex)
            item(entry) {
                model(basicItem("metaitems/$texName"))
            }
        }
    }

    private fun circuits() {
        itemData {
            for (circuit in Circuits.CIRCUITS) {
                item(circuit.entry) {
                    model(basicItem("metaitems/${circuit.entry.id().replace('/', '.')}"))
                    tag(AllTags.circuit(Circuits.getVoltage(circuit.tier, circuit.level)))
                }
            }

            for (component in Circuits.COMPONENTS.values) {
                for (tier in CircuitComponentTier.entries) {
                    val name = component.name
                    val entry = component.entry(tier)
                    val texKey = if (tier.prefix.isEmpty()) name else "${tier.prefix}.$name"
                    item(entry) {
                        model(basicItem("metaitems/component.$texKey"))
                        for (tier1 in CircuitComponentTier.entries) {
                            if (tier1.rank <= tier.rank) {
                                tag(AllTags.circuitComponent(name, tier1))
                            }
                        }
                    }
                }
            }

            for (tier in CircuitTier.entries) {
                val boardName = when (tier) {
                    NANO -> "epoxy"
                    QUANTUM -> "fiber_reinforced"
                    CRYSTAL -> "multilayer.fiber_reinforced"
                    else -> tier.board
                }

                item(board(tier)) {
                    model(basicItem("metaitems/board.$boardName"))
                }

                item(circuitBoard(tier)) {
                    model(basicItem("metaitems/circuit_board.${tier.circuitBoard}"))
                }
            }
        }
    }

    private fun tools() {
        itemData {
            for ((v, entry) in FLUID_CELL) {
                val texKey = if (v == ULV) {
                    "metaitems/fluid_cell"
                } else {
                    "metaitems/large_fluid_cell.${name(entry.id(), -1)}"
                }
                item(entry) { model(basicItem("$texKey/base", "$texKey/overlay")) }
            }
        }
    }
}
