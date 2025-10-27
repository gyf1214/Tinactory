package org.shsts.tinactory.datagen.content.component

import net.minecraft.world.item.Item
import org.shsts.tinactory.content.AllItems.STORAGE_CELLS
import org.shsts.tinactory.content.AllItems.componentEntry
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WIRE_CUTTER
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.CircuitTier.CRYSTAL
import org.shsts.tinactory.content.electric.CircuitTier.NANO
import org.shsts.tinactory.content.electric.CircuitTier.QUANTUM
import org.shsts.tinactory.content.electric.Circuits.BOULE_LIST
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.content.electric.Circuits.WAFER
import org.shsts.tinactory.content.electric.Circuits.WAFER_RAW_LIST
import org.shsts.tinactory.content.electric.Circuits.allCircuitComponents
import org.shsts.tinactory.content.electric.Circuits.allCircuits
import org.shsts.tinactory.content.electric.Circuits.board
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.content.network.CableBlock
import org.shsts.tinactory.content.tool.BatteryItem
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.ae2
import org.shsts.tinactory.core.util.LocHelper.ic2
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.machineItem
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.itemData
import org.shsts.tinactory.datagen.content.builder.ItemDataFactory
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

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
        for (entry in componentEntry<CableBlock>("cable").values) {
            blockData(entry) {
                blockState(Models::cableBlock)
                itemModel(Models::cableItem)
                tag(MINEABLE_WITH_WIRE_CUTTER)
            }
        }

        itemData {
            val components = listOf(
                "electric_motor",
                "electric_pump",
                "electric_piston",
                "conveyor_module",
                "robot_arm",
                "sensor",
                "emitter",
                "field_generator")
            for (entry in components.flatMap { componentEntry<Item>(it).values }) {
                item(entry) { model(Models::componentItem) }
            }

            for ((v, entry) in componentEntry<BatteryItem>("battery")) {
                item(entry) {
                    model(Models::batteryItem)
                    tag(AllTags.battery(v))
                }
            }

            for ((v, entry) in componentEntry<Item>("machine_hull")) {
                item(entry) { model(machineItem(v, IO_TEX)) }
            }

            for (entry in componentEntry<Item>("research_equipment").values) {
                item(entry) { model(basicItem("${RESEARCH_TEX}base", "${RESEARCH_TEX}overlay")) }
            }

            component("grinder/good") { model(basicItem("$GRINDER_TEX.diamond")) }
            component("grinder/advanced") { model(basicItem("$GRINDER_TEX.tungsten")) }

            for (entry in componentEntry<Item>("buzzsaw").values) {
                item(entry) { model(basicItem("tools/buzzsaw")) }
            }

            for (id in listOf("component/item_filter", "misc/fertilizer", "misc/gelled_toluene")) {
                item(id) { model(Models::simpleItem) }
            }

            component("mixed_metal_ingot") {
                model(basicItem(ic2("items/resource/ingot/alloy")))
            }

            component("advanced_alloy") {
                model(basicItem(ic2("items/crafting/alloy")))
            }

            component("annihilation_core") {
                model(basicItem(ae2("items/material_annihilation_core")))
            }

            component("formation_core") {
                model(basicItem(ae2("items/material_formation_core")))
            }

            component("carbon_fiber") {
                model(basicItem("metaitems/carbon.fibres"))
            }

            component("carbon_mesh") {
                model(basicItem("metaitems/carbon.mesh"))
            }

            component("carbon_plate") {
                model(basicItem("metaitems/carbon.plate"))
            }

            component("quantum_eye") {
                model(basicItem("metaitems/quantumeye"))
            }

            for (entry in STORAGE_CELLS) {
                val k = name(entry.component.id(), -1).replace('m', 'k')
                item(entry.component) {
                    model(basicItem(ae2("items/material_cell${k}_part")))
                }
                item(entry.item) {
                    model(basicItem(ae2("items/storage_cell_${k}")))
                    tag(AllTags.ITEM_STORAGE_CELL)
                }
                item(entry.fluid) {
                    model(basicItem(ae2("items/fluid_storage_cell_${k}")))
                    tag(AllTags.FLUID_STORAGE_CELL)
                }
            }
        }
    }

    private fun ItemDataFactory.component(id: String, block: IItemDataBuilder<Item, *>.() -> Unit) {
        item("component/$id", block)
    }

    private fun chips() {
        itemData {
            for (entry in BOULE_LIST + WAFER_RAW_LIST) {
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
        for (entry in listOf(WAFER.getValue(name), CHIP.getValue(name))) {
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
            for (circuit in allCircuits()) {
                item(circuit.entry) {
                    model(basicItem("metaitems/${circuit.entry.id().replace('/', '.')}"))
                    tag(AllTags.circuit(circuit.voltage))
                }
            }

            for (component in allCircuitComponents()) {
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
            for ((v, entry) in componentEntry<Item>("fluid_cell")) {
                val texKey = when (v) {
                    Voltage.ULV -> "metaitems/fluid_cell"
                    Voltage.IV -> "metaitems/large_fluid_cell.tungstensteel"
                    else -> "metaitems/large_fluid_cell.${name(entry.id(), -1)}"
                }
                item(entry) { model(basicItem("$texKey/base", "$texKey/overlay")) }
            }
        }
    }
}
