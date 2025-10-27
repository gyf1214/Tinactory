package org.shsts.tinactory.datagen.content.component

import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.BOULE
import org.shsts.tinactory.content.electric.Circuits.BOULE_LIST
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.content.electric.Circuits.WAFER
import org.shsts.tinactory.content.electric.Circuits.WAFER_RAW
import org.shsts.tinactory.content.electric.Circuits.WAFER_RAW_LIST
import org.shsts.tinactory.content.electric.Circuits.board
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.content.electric.Circuits.getCircuit
import org.shsts.tinactory.content.electric.Circuits.getCircuitComponent
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.circuitAssembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.laserEngraver
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.SimpleProcessingBuilder
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS
import kotlin.math.max

object CircuitComponents {
    private const val CIRCUIT_TICKS = 200L

    fun init() {
        ulvCircuits()
        circuits()
        boards()
        chips()
        circuitComponents()
    }

    private fun ulvCircuits() {
        val vacuumTube = getCircuit("vacuum_tube").item
        val electronic = getCircuit("electronic").item

        vanilla {
            shaped(vacuumTube) {
                pattern("BGB")
                pattern("WWW")
                define('G', "glass", "primary")
                define('W', "copper", "wire")
                define('B', "iron", "bolt")
                unlockedBy("has_wire", "copper", "wire")
            }

            shaped(electronic) {
                val board = circuitBoard(CircuitTier.ELECTRONIC).get()
                pattern("RPR")
                pattern("TBT")
                pattern("WWW")
                define('R', getCircuitComponent("resistor").item(CircuitComponentTier.NORMAL))
                define('P', "steel", "plate")
                define('T', vacuumTube)
                define('B', board)
                define('W', "red_alloy", "wire")
                unlockedBy("has_board", board)
            }

            shaped(getCircuit("good_electronic").item) {
                pattern("DPD")
                pattern("EBE")
                pattern("WEW")
                define('D', getCircuitComponent("diode").item(CircuitComponentTier.NORMAL))
                define('P', "steel", "plate")
                define('E', electronic)
                define('B', circuitBoard(CircuitTier.ELECTRONIC).get())
                define('W', "copper", "wire")
                unlockedBy("has_circuit", electronic)
            }
        }

        assembler {
            output(vacuumTube) {
                input("glass", "primary")
                input("copper", "wire")
                input("iron", "bolt")
                voltage(Voltage.ULV)
                workTicks(120)
                tech(Technologies.SOLDERING)
            }
        }
    }

    private fun circuits() {
        circuitTier(CircuitTier.ELECTRONIC) {
            circuitAssembler {
                circuit("electronic") {
                    circuit("vacuum_tube", 2)
                    component("resistor", 2)
                    input("red_alloy", "wire", 2)
                }
                circuit("good_electronic") {
                    circuit("electronic", 2)
                    component("diode", 2)
                    input("copper", "wire", 2)
                }
            }
        }

        circuitTier(CircuitTier.INTEGRATED) {
            circuitAssembler {
                circuit("basic_integrated") {
                    chip("integrated_circuit")
                    component("resistor", 2)
                    component("diode", 2)
                    input("copper", "wire_fine", 2)
                    input("tin", "bolt", 2)
                }
                circuit("good_integrated") {
                    circuit("basic_integrated", 2)
                    component("resistor", 2)
                    component("diode", 2)
                    input("gold", "wire_fine", 4)
                    input("silver", "bolt", 4)
                }
                circuit("advanced_integrated") {
                    circuit("good_integrated", 2)
                    chip("integrated_circuit", 2)
                    chip("ram", 2)
                    component("transistor", 4)
                    input("electrum", "wire_fine", 8)
                    input("copper", "bolt", 8)
                }
            }
        }

        circuitTier(CircuitTier.CPU) {
            circuitAssembler {
                circuit("nand_chip", 8) {
                    chip("cpu")
                    input("copper", "wire_fine", 2)
                    input("tin", "bolt", 2)
                }
                circuit("microprocessor", 3) {
                    chip("cpu")
                    component("resistor", 2)
                    component("capacitor", 2)
                    component("transistor", 2)
                    input("copper", "wire_fine", 2)
                }
                circuit("processor") {
                    chip("cpu")
                    component("resistor", 4)
                    component("capacitor", 4)
                    component("transistor", 4)
                    input("red_alloy", "wire_fine", 4)
                }
                circuit("assembly") {
                    circuit("processor", 2)
                    component("inductor", 4)
                    component("capacitor", 8)
                    chip("ram", 4)
                    input("red_alloy", "wire_fine", 8)
                }
                circuit("workstation") {
                    circuit("assembly", 2)
                    component("resistor", 4)
                    component("diode", 4)
                    chip("ram", 4)
                    input("electrum", "wire_fine", 16)
                    input("gold", "bolt", 16)
                }
                circuit("mainframe") {
                    input("aluminium", "stick", 8)
                    circuit("workstation", 2)
                    component("inductor", 8)
                    component("capacitor", 16)
                    chip("nor", 8)
                    input("annealed_copper", "wire", 16)
                }
            }
        }

        circuitTier(CircuitTier.NANO) {
            circuitAssembler {
                circuit("nano_processor") {
                    chip("nano_cpu")
                    component("resistor", 4)
                    component("capacitor", 4)
                    component("transistor", 4)
                    input("electrum", "wire_fine", 4)
                }
                circuit("nano_assembly") {
                    circuit("nano_processor", 2)
                    component("inductor", 4)
                    component("capacitor", 8)
                    chip("ram", 4)
                    input("electrum", "wire_fine", 8)
                }
                circuit("nano_computer") {
                    circuit("nano_assembly", 2)
                    component("resistor", 4)
                    component("diode", 4)
                    chip("nor", 4)
                    input("annealed_copper", "wire_fine", 16)
                    input("gold", "bolt", 16)
                }
                circuit("nano_mainframe") {
                    input("titanium", "stick", 8)
                    circuit("nano_computer", 2)
                    component("inductor", 8)
                    component("capacitor", 16)
                    chip("nor", 16)
                    input("annealed_copper", "wire", 16)
                }
            }
        }
    }

    private fun circuitComponents() {
        componentTier(CircuitComponentTier.NORMAL) {
            val resin = getItem("rubber_tree/sticky_resin")
            vanilla {
                shaped(getCircuitComponent("resistor").item(tier)) {
                    pattern(" R ")
                    pattern("WCW")
                    pattern(" R ")
                    define('R', resin)
                    define('W', "copper", "wire")
                    define('C', "coal", "dust")
                    unlockedBy("has_resin", resin)
                }
            }

            assembler {
                defaults {
                    workTicks(COMPONENT_TICKS)
                    tech(Technologies.SOLDERING)
                }
                component("resistor", 2) {
                    input("coal", "dust")
                    input("copper", "wire_fine", 4)
                    input("rubber")
                    voltage(Voltage.ULV)
                }
                component("diode", 4) {
                    input("gallium_arsenide", "dust")
                    input("copper", "wire_fine", 4)
                    input("rubber", amount = 2)
                    voltage(Voltage.LV)
                }
            }

            assembler {
                defaults {
                    voltage(Voltage.LV)
                    workTicks(COMPONENT_TICKS)
                    tech(Technologies.INTEGRATED_CIRCUIT)
                }
                component("capacitor", 8) {
                    input("pvc", "foil")
                    input("aluminium", "foil", 2)
                    input("pe")
                }
                component("inductor", 4) {
                    input("nickel_zinc_ferrite", "ring")
                    input("copper", "wire_fine", 2)
                    input("pe", amount = 0.25)
                }
                component("diode", 8, suffix = "_from_wafer") {
                    input(WAFER_RAW.item("silicon"))
                    input("copper", "wire_fine", 4)
                    input("pe")
                }
                component("transistor", 4) {
                    input("gallium_arsenide", "dust")
                    input("tin", "wire_fine", 6)
                    input("rubber", amount = 2)
                }
                component("transistor", 8, suffix = "_from_wafer") {
                    input(WAFER_RAW.item("silicon"))
                    input("tin", "wire_fine", 6)
                    input("pe")
                }
            }
        }

        componentTier(CircuitComponentTier.SMD) {
            assembler {
                defaults {
                    voltage(Voltage.HV)
                    workTicks(COMPONENT_TICKS)
                    tech(Technologies.SURFACE_MOUNT_DEVICE)
                }
                component("resistor", 16) {
                    input("carbon", "dust")
                    input("electrum", "wire_fine", 4)
                    input("pe")
                }
                component("diode", 32) {
                    input("gallium_arsenide", "dust")
                    input("platinum", "wire_fine", 4)
                    input("pe", amount = 2)
                }
                component("transistor", 16) {
                    input("gallium", "foil")
                    input("annealed_copper", "wire_fine", 4)
                    input("pe")
                }
                component("capacitor", 12) {
                    input("pvc", "foil", 2)
                    input("aluminium", "foil")
                    input("pe", amount = 0.75)
                }
                component("inductor", 16) {
                    input("nickel_zinc_ferrite", "ring")
                    input("annealed_copper", "wire_fine", 8)
                    input("pe")
                }
            }
        }
    }

    private class ComponentTierFactory(val tier: CircuitComponentTier) {
        fun AssemblyRecipeFactory.component(name: String, amount: Int,
            suffix: String = "", block: AssemblyRecipeBuilder.() -> Unit) {
            output(getCircuitComponent(name).item(tier), amount, suffix, block = block)
        }
    }

    private fun componentTier(tier: CircuitComponentTier, block: ComponentTierFactory.() -> Unit) {
        ComponentTierFactory(tier).apply(block)
    }

    private fun chips() {
        blastFurnace {
            output(BOULE.item("silicon")) {
                input("silicon", amount = 32)
                input("gallium_arsenide")
                voltage(Voltage.LV)
                workTicks(6400)
                extra {
                    temperature(2100)
                }
            }
            output(BOULE.item("glowstone")) {
                input("silicon", amount = 64)
                input("glowstone")
                input("nitrogen", amount = 6)
                voltage(Voltage.HV)
                workTicks(9600)
                extra {
                    temperature(3000)
                }
            }
        }

        cutter {
            for ((i, entry) in WAFER_RAW_LIST.withIndex()) {
                output(entry.get(), 8 shl i) {
                    input(BOULE_LIST.item(i))
                    input("water", amount = 1 shl i)
                    voltage(Voltage.fromRank(2 + 2 * i))
                    workTicks(400L shl i)
                }
            }
            for ((key, entry) in CHIP) {
                output(entry.get(), 6) {
                    input(WAFER.item(key))
                    input("water", amount = 0.75)
                    voltage(Voltage.LV)
                    workTicks(300)
                }
            }
        }

        engraving("integrated_circuit", "ruby", 0, Voltage.LV, -1.0, 0.0)
        engraving("cpu", "diamond", 0, Voltage.MV, 0.0, 1.0)
        engraving("ram", "sapphire", 0, Voltage.MV, -0.5, 0.5)
        engraving("low_pic", "emerald", 0, Voltage.MV, -0.3, 0.7)
        engraving("nano_cpu", "emerald", 1, Voltage.HV, 0.25, 1.25, "cpu")
        engraving("nor", "diamond", 1, Voltage.HV, 0.1, 1.1, "ram")
        engraving("simple_soc", "blue_topaz", 1, Voltage.HV, 0.15, 1.15)
        engraving("pic", "topaz", 1, Voltage.HV, 0.2, 1.2)
    }

    private fun engraving(name: String, lens: String, level: Int, voltage: Voltage,
        minCleanness: Double, maxCleanness: Double, source: String? = null) {
        val wafer = WAFER.item(name)

        laserEngraver {
            defaults {
                input(lens, "lens", 0, port = 1)
                workTicks(1000L shl level)
                extra {
                    requireCleanness(minCleanness, maxCleanness)
                }
            }

            if (source != null) {
                output(wafer) {
                    input(WAFER.item(source))
                    voltage(voltage)
                }
            } else {
                for (i in level..<WAFER_RAW_LIST.size) {
                    val j = i - level
                    val rawWafer = WAFER_RAW_LIST.item(i)
                    val rawId = name(rawWafer.asItem().registryName!!.path, -1)
                    output(wafer, 1 shl j, suffix = "_from_$rawId") {
                        input(rawWafer)
                        voltage(Voltage.fromRank(voltage.rank + j))
                    }
                }
            }
        }
    }

    private fun boards() {
        circuitTier(CircuitTier.ELECTRONIC) {
            val resin = getItem("rubber_tree/sticky_resin")
            vanilla {
                shaped(board, 3) {
                    pattern("SSS")
                    pattern("WWW")
                    pattern("SSS")
                    define('S', resin)
                    define('W', ItemTags.PLANKS)
                    unlockedBy("has_resin", resin)
                }

                shaped(circuitBoard) {
                    pattern("WWW")
                    pattern("WBW")
                    pattern("WWW")
                    define('B', board)
                    define('W', "copper", "wire")
                    unlockedBy("has_board", board)
                }
            }

            assembler {
                defaults {
                    voltage(Voltage.ULV)
                    workTicks(CIRCUIT_TICKS)
                    tech(Technologies.SOLDERING)
                }
                output(board) {
                    input(ItemTags.PLANKS)
                    input("rubber", amount = 2)
                }
                output(circuitBoard) {
                    input(board)
                    input("copper", "wire_fine", 8)
                    input("soldering_alloy", amount = 0.5)
                }
            }
        }

        circuitTier(CircuitTier.INTEGRATED) {
            assembler {
                defaults {
                    voltage(Voltage.LV)
                    workTicks(CIRCUIT_TICKS)
                    tech(Technologies.INTEGRATED_CIRCUIT)
                }
                output(board) {
                    input(lastBoard, 2)
                    input("red_alloy", "wire_fine", 8)
                    input("soldering_alloy")
                }
                output(circuitBoard) {
                    input(board)
                    input("silver", "wire_fine", 8)
                    input("soldering_alloy", amount = 0.5)
                }
            }
        }

        circuitTier(CircuitTier.CPU) {
            chemicalReactor {
                defaults {
                    input("sulfuric_acid", "dilute", 0.25)
                    voltage(Voltage.MV)
                    workTicks(240)
                    tech(Technologies.CPU)
                }
                output(board) {
                    input("pe", "sheet")
                    input("copper", "foil", 4)
                }
                output(board, 2, suffix = "_from_pvc") {
                    input("pvc", "sheet")
                    input("copper", "foil", 4)
                }
            }
            chemicalReactor {
                output(circuitBoard) {
                    input(board)
                    input("copper", "foil", 6)
                    input("iron_chloride", amount = 0.25)
                    voltage(Voltage.MV)
                    workTicks(320)
                    tech(Technologies.CPU)
                }
            }
        }

        circuitTier(CircuitTier.NANO) {
            assembler {
                output(board) {
                    input(lastBoard, 3)
                    input("silver", "wire_fine", 8)
                    input("soldering_alloy", amount = 1.5)
                    voltage(Voltage.MV)
                    workTicks(CIRCUIT_TICKS)
                    tech(Technologies.CPU)
                }
            }
            chemicalReactor {
                output(circuitBoard) {
                    input(board)
                    input("electrum", "foil", 12)
                    input("iron_chloride", amount = 0.5)
                    voltage(Voltage.MV)
                    workTicks(480)
                    tech(Technologies.CPU)
                }
            }
        }

        circuitTier(CircuitTier.QUANTUM) {
            chemicalReactor {
                defaults {
                    voltage(Voltage.HV)
                    // TODO
                    tech()
                }
                output(board) {
                    input("epoxy", "sheet")
                    input("annealed_copper", "foil", 8)
                    input("sulfuric_acid", "dilute", 0.5)
                    workTicks(320)
                }
                output(circuitBoard) {
                    input(board)
                    input("annealed_copper", "foil", 16)
                    input("iron_chloride", amount = 0.75)
                    workTicks(480)
                }
            }
        }

        circuitTier(CircuitTier.CRYSTAL) {
            assembler {
                defaults {
                }
                output(board) {
                    input(lastBoard, 4)
                    input("platinum", "wire_fine", 16)
                    input("soldering_alloy", amount = 2)
                    voltage(Voltage.EV)
                    workTicks(CIRCUIT_TICKS)
                    // TODO
                    tech()
                }
            }
            chemicalReactor {
                output(circuitBoard) {
                    input(board)
                    input("platinum", "foil", 16)
                    input("iron_chloride")
                    voltage(Voltage.EV)
                    workTicks(480)
                    // TODO
                    tech()
                }
            }
        }
    }

    private class CircuitTierFactory(val tier: CircuitTier) {
        val componentTier = tier.componentTier

        val board = board(tier).get()

        val lastBoard: Item by lazy { board(CircuitTier.fromRank(tier.rank - 1)).get() }

        val circuitBoard = circuitBoard(tier).get()

        fun SimpleProcessingBuilder.component(name: String, amount: Int = 1) {
            input(getCircuitComponent(name).tag(componentTier), amount)
        }

        fun SimpleProcessingBuilder.circuit(name: String, amount: Int = 1) {
            input(getCircuit(name).item, amount)
        }

        fun SimpleProcessingBuilder.chip(name: String, amount: Int = 1) {
            input(CHIP.item(name), amount)
        }

        fun ProcessingRecipeFactory.circuit(name: String, amount: Int = 1,
            block: SimpleProcessingBuilder.() -> Unit) {
            val circuit = getCircuit(name)
            output(circuit.item, amount) {
                if (circuit.level().voltageOffset < 2) {
                    input(circuit.circuitBoard)
                }
                block()
                val voltage = circuit.tier().baseVoltage
                val voltage1 = if (voltage.rank < Voltage.LV.rank) Voltage.LV else voltage
                val level = 1 + max(0, circuit.level().voltageOffset)
                val solder = (1 shl (level - 1)) / 2.0
                input("soldering_alloy", amount = solder)
                voltage(voltage1)
                workTicks(200L * level)
            }
        }
    }

    private fun circuitTier(tier: CircuitTier, block: CircuitTierFactory.() -> Unit) {
        CircuitTierFactory(tier).apply(block)
    }
}
