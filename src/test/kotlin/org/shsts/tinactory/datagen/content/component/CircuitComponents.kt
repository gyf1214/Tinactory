package org.shsts.tinactory.datagen.content.component

import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import org.shsts.tinactory.content.AllItems.ADVANCED_INTEGRATED
import org.shsts.tinactory.content.AllItems.BASIC_INTEGRATED
import org.shsts.tinactory.content.AllItems.BOULES
import org.shsts.tinactory.content.AllItems.CAPACITOR
import org.shsts.tinactory.content.AllItems.CHIPS
import org.shsts.tinactory.content.AllItems.DIODE
import org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT
import org.shsts.tinactory.content.AllItems.GOOD_ELECTRONIC
import org.shsts.tinactory.content.AllItems.GOOD_INTEGRATED
import org.shsts.tinactory.content.AllItems.INDUCTOR
import org.shsts.tinactory.content.AllItems.INTEGRATED_PROCESSOR
import org.shsts.tinactory.content.AllItems.MAINFRAME
import org.shsts.tinactory.content.AllItems.MICROPROCESSOR
import org.shsts.tinactory.content.AllItems.PROCESSOR_ASSEMBLY
import org.shsts.tinactory.content.AllItems.RAW_WAFERS
import org.shsts.tinactory.content.AllItems.RESISTOR
import org.shsts.tinactory.content.AllItems.STICKY_RESIN
import org.shsts.tinactory.content.AllItems.TRANSISTOR
import org.shsts.tinactory.content.AllItems.VACUUM_TUBE
import org.shsts.tinactory.content.AllItems.WAFERS
import org.shsts.tinactory.content.AllItems.WORKSTATION
import org.shsts.tinactory.content.electric.Circuit
import org.shsts.tinactory.content.electric.CircuitComponent
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.board
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.name
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
        circuits()
        boards()
        chips()
        circuitComponents()
    }

    private fun circuits() {
        vanilla {
            shaped(VACUUM_TUBE.item) {
                pattern("BGB")
                pattern("WWW")
                define('G', "glass", "primary")
                define('W', "copper", "wire")
                define('B', "iron", "bolt")
                unlockedBy("has_wire", "copper", "wire")
            }

            shaped(ELECTRONIC_CIRCUIT.item) {
                val board = circuitBoard(CircuitTier.ELECTRONIC).get()
                pattern("RPR")
                pattern("TBT")
                pattern("WWW")
                define('R', RESISTOR.item(CircuitComponentTier.NORMAL))
                define('P', "steel", "plate")
                define('T', VACUUM_TUBE.item)
                define('B', board)
                define('W', "red_alloy", "wire")
                unlockedBy("has_board", board)
            }

            shaped(GOOD_ELECTRONIC.item) {
                val circuit = ELECTRONIC_CIRCUIT.item
                pattern("DPD")
                pattern("EBE")
                pattern("WEW")
                define('D', DIODE.item(CircuitComponentTier.NORMAL))
                define('P', "steel", "plate")
                define('E', circuit)
                define('B', circuitBoard(CircuitTier.ELECTRONIC).get())
                define('W', "copper", "wire")
                unlockedBy("has_circuit", circuit)
            }
        }

        assembler {
            output(VACUUM_TUBE.item) {
                input("glass", "primary")
                input("copper", "wire")
                input("iron", "bolt")
                voltage(Voltage.ULV)
                workTicks(120)
                tech(Technologies.SOLDERING)
            }
        }

        circuitTier(CircuitTier.ELECTRONIC) {
            circuitAssembler {
                output(ELECTRONIC_CIRCUIT) {
                    input(VACUUM_TUBE, 2)
                    input(RESISTOR, 2)
                    input("red_alloy", "wire", 2)
                }
                output(GOOD_ELECTRONIC) {
                    input(ELECTRONIC_CIRCUIT, 2)
                    input(DIODE, 2)
                    input("copper", "wire", 2)
                }
            }
        }

        circuitTier(CircuitTier.INTEGRATED) {
            circuitAssembler {
                output(BASIC_INTEGRATED) {
                    chip("integrated_circuit")
                    input(RESISTOR, 2)
                    input(DIODE, 2)
                    input("copper", "wire_fine", 2)
                    input("tin", "bolt", 2)
                }
                output(GOOD_INTEGRATED) {
                    input(BASIC_INTEGRATED, 2)
                    input(RESISTOR, 2)
                    input(DIODE, 2)
                    input("gold", "wire_fine", 4)
                    input("silver", "bolt", 4)
                }
                output(ADVANCED_INTEGRATED) {
                    input(GOOD_INTEGRATED, 2)
                    chip("integrated_circuit", 2)
                    chip("ram", 2)
                    input(TRANSISTOR, 4)
                    input("electrum", "wire_fine", 8)
                    input("copper", "bolt", 8)
                }
            }
        }

        circuitTier(CircuitTier.CPU) {
            circuitAssembler {
                output(MICROPROCESSOR, 3) {
                    chip("cpu")
                    input(RESISTOR, 2)
                    input(CAPACITOR, 2)
                    input(TRANSISTOR, 2)
                    input("copper", "wire_fine", 2)
                }
                output(INTEGRATED_PROCESSOR) {
                    chip("cpu")
                    input(RESISTOR, 2)
                    input(CAPACITOR, 2)
                    input(TRANSISTOR, 2)
                    input("red_alloy", "wire_fine", 4)
                }
                output(PROCESSOR_ASSEMBLY) {
                    input(INTEGRATED_PROCESSOR, 2)
                    input(INDUCTOR, 2)
                    input(CAPACITOR, 8)
                    chip("ram", 4)
                    input("red_alloy", "wire_fine", 8)
                }
                output(WORKSTATION) {
                    input(PROCESSOR_ASSEMBLY, 2)
                    input(DIODE, 4)
                    chip("ram", 4)
                    input("electrum", "wire_fine", 16)
                    input("gold", "bolt", 16)
                }
                output(MAINFRAME) {
                    input("aluminium", "stick", 8)
                    input(WORKSTATION, 2)
                    chip("ram", 16)
                    input(INDUCTOR, 8)
                    input(CAPACITOR, 16)
                    input("copper", "wire", 16)
                }
            }
        }
    }

    private fun circuitComponents() {
        componentTier(CircuitComponentTier.NORMAL) {
            vanilla {
                shaped(RESISTOR.item(tier)) {
                    pattern(" R ")
                    pattern("WCW")
                    pattern(" R ")
                    define('R', STICKY_RESIN.get())
                    define('W', "copper", "wire")
                    define('C', "coal", "dust")
                    unlockedBy("has_resin", STICKY_RESIN.get())
                }
            }

            assembler {
                defaults {
                    workTicks(COMPONENT_TICKS)
                    tech(Technologies.SOLDERING)
                }
                output(RESISTOR, 2) {
                    input("coal", "dust", 1)
                    input("copper", "wire_fine", 4)
                    input("rubber")
                    voltage(Voltage.ULV)
                }
                output(DIODE, 4) {
                    input("gallium_arsenide", "dust")
                    input("glass", "primary")
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
                output(CAPACITOR, 8) {
                    input("pvc", "foil")
                    input("aluminium", "foil", 2)
                    input("pe")
                }
                output(INDUCTOR, 4) {
                    input("nickel_zinc_ferrite", "ring")
                    input("copper", "wire_fine", 2)
                    input("pe", amount = 0.25)
                }
                output(DIODE, 8, suffix = "_from_wafer") {
                    input(RAW_WAFERS.item(0))
                    input("copper", "wire_fine", 4)
                    input("pe")
                }
                output(TRANSISTOR, 4) {
                    input("gallium_arsenide", "dust")
                    input("tin", "wire_fine", 6)
                    input("rubber", amount = 2)
                }
                output(TRANSISTOR, 8, suffix = "_from_pe") {
                    input("silicon", "dust")
                    input("tin", "wire_fine", 6)
                    input("pe")
                }
            }
        }
    }

    private class ComponentTierFactory(val tier: CircuitComponentTier) {
        fun AssemblyRecipeFactory.output(component: CircuitComponent, amount: Int,
            suffix: String = "", block: AssemblyRecipeBuilder.() -> Unit) {
            output(component.item(tier), amount, suffix, block = block)
        }
    }

    private fun componentTier(tier: CircuitComponentTier, block: ComponentTierFactory.() -> Unit) {
        ComponentTierFactory(tier).apply(block)
    }

    private fun chips() {
        blastFurnace {
            output(BOULES.item(0)) {
                input("silicon", amount = 32)
                input("gallium_arsenide")
                voltage(Voltage.LV)
                workTicks(6400)
                extra {
                    temperature(2100)
                }
            }
        }

        cutter {
            for ((i, entry) in RAW_WAFERS.withIndex()) {
                output(entry.get(), 8 shl i) {
                    input(BOULES.item(i))
                    input("water", amount = 1 shl i)
                    voltage(Voltage.fromRank(2 + 2 * i))
                    workTicks(400L shl i)
                }
            }
            for ((key, entry) in CHIPS) {
                output(entry.get(), 6) {
                    input(WAFERS.item(key))
                    input("water", amount = 0.75)
                    voltage(Voltage.LV)
                    workTicks(300)
                }
            }
        }

        engraving("integrated_circuit", "ruby", 0, Voltage.LV, -1.0, 0.0)
        engraving("cpu", "diamond", 0, Voltage.MV, 0.0, 0.5)
        engraving("ram", "sapphire", 0, Voltage.MV, -0.25, 0.25)
        engraving("low_pic", "emerald", 0, Voltage.MV, -0.3, 0.2)
    }

    private fun engraving(name: String, lens: String, level: Int, voltage: Voltage,
        minCleanness: Double, maxCleanness: Double) {
        val wafer = WAFERS.item(name)
        for (i in level..<RAW_WAFERS.size) {
            val j = i - level
            val rawWafer = RAW_WAFERS.item(i)
            val rawId = name(rawWafer.asItem().registryName!!.path, -1)
            val minC = 1 - (1 - minCleanness) / (1 shl i)
            val maxC = 1 - (1 - maxCleanness) / (1 shl i)
            laserEngraver {
                output(wafer, 1 shl j, suffix = "_from_$rawId") {
                    input(rawWafer)
                    input(lens, "lens", 0, port = 1)
                    voltage(Voltage.fromRank(voltage.rank + j))
                    workTicks(1000L shl level)
                    extra {
                        requireCleanness(minC, maxC)
                    }
                }
            }
        }
    }

    private fun boards() {
        circuitTier(CircuitTier.ELECTRONIC) {
            val resin = STICKY_RESIN.get()

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
                    input(resin, 2)
                }
                output(circuitBoard) {
                    input(board)
                    input("copper", "wire", 8)
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
                    input("red_alloy", "wire", 8)
                    input("soldering_alloy")
                }
                output(circuitBoard) {
                    input(board)
                    input("silver", "wire", 8)
                    input("soldering_alloy", amount = 0.5)
                }
            }
        }

        circuitTier(CircuitTier.CPU) {
            chemicalReactor {
                defaults {
                    input("copper", "foil", 4)
                    input("sulfuric_acid", "dilute", 0.25)
                    voltage(Voltage.MV)
                    workTicks(240)
                    tech(Technologies.CPU)
                }
                output(board) {
                    input("pe", "sheet")
                }
                output(board, 2, suffix = "_from_pvc") {
                    input("pvc", "sheet")
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
    }

    private class CircuitTierFactory(val tier: CircuitTier) {
        val componentTier = tier.componentTier

        val board = board(tier).get()

        val lastBoard: Item by lazy { board(CircuitTier.fromRank(tier.rank - 1)).get() }

        val circuitBoard = circuitBoard(tier).get()

        fun SimpleProcessingBuilder.input(component: CircuitComponent, amount: Int = 1) {
            input(component.tag(componentTier), amount)
        }

        fun SimpleProcessingBuilder.input(circuit: Circuit, amount: Int = 1) {
            input(circuit.item, amount)
        }

        fun SimpleProcessingBuilder.chip(name: String, amount: Int = 1) {
            input(CHIPS.item(name), amount)
        }

        fun ProcessingRecipeFactory.output(circuit: Circuit, amount: Int = 1,
            block: SimpleProcessingBuilder.() -> Unit) {
            output(circuit.item, amount) {
                if (circuit.level().voltageOffset < 2) {
                    input(circuit.circuitBoard())
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
