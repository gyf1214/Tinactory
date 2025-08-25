package org.shsts.tinactory.datagen.content.component

import org.shsts.tinactory.content.AllItems.BOULES
import org.shsts.tinactory.content.AllItems.CAPACITOR
import org.shsts.tinactory.content.AllItems.CHIPS
import org.shsts.tinactory.content.AllItems.DIODE
import org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT
import org.shsts.tinactory.content.AllItems.GOOD_ELECTRONIC
import org.shsts.tinactory.content.AllItems.INDUCTOR
import org.shsts.tinactory.content.AllItems.RAW_WAFERS
import org.shsts.tinactory.content.AllItems.RESISTOR
import org.shsts.tinactory.content.AllItems.STICKY_RESIN
import org.shsts.tinactory.content.AllItems.TRANSISTOR
import org.shsts.tinactory.content.AllItems.VACUUM_TUBE
import org.shsts.tinactory.content.AllItems.WAFERS
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllRecipes.has
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.CircuitComponent
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.component.Components.ASSEMBLY_TICKS

object CircuitComponents {
    fun init() {
        circuits()
        circuitComponents()
        chips()
    }

    private fun circuits() {
        vanilla {
            shaped(VACUUM_TUBE.item) {
                val wire = getMaterial("copper").tag("wire")
                pattern("BGB")
                pattern("WWW")
                define('G', getMaterial("glass").tag("primary"))
                define('W', wire)
                define('B', getMaterial("iron").tag("bolt"))
                unlockedBy("has_wire", has(wire))
            }

            shaped(ELECTRONIC_CIRCUIT.item) {
                val board = circuitBoard(CircuitTier.ELECTRONIC).get()
                pattern("RPR")
                pattern("TBT")
                pattern("WWW")
                define('R', RESISTOR.item(CircuitComponentTier.NORMAL))
                define('P', getMaterial("steel").tag("plate"))
                define('T', VACUUM_TUBE.item)
                define('B', board)
                define('W', getMaterial("red_alloy").tag("wire"))
                unlockedBy("has_board", has(board))
            }

            shaped(GOOD_ELECTRONIC.item) {
                val circuit = ELECTRONIC_CIRCUIT.item
                pattern("DPD")
                pattern("EBE")
                pattern("WEW")
                define('D', DIODE.item(CircuitComponentTier.NORMAL))
                define('P', getMaterial("steel").tag("plate"))
                define('E', circuit)
                define('B', circuitBoard(CircuitTier.ELECTRONIC).get())
                define('W', getMaterial("copper").tag("wire"))
                unlockedBy("has_circuit", has(circuit))
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
    }

    private fun circuitComponents() {
        componentTier(CircuitComponentTier.NORMAL) {
            vanilla {
                shaped(RESISTOR.item(tier)) {
                    pattern(" R ")
                    pattern("WCW")
                    pattern(" R ")
                    define('R', STICKY_RESIN.get())
                    define('W', getMaterial("copper").tag("wire"))
                    define('C', getMaterial("coal").tag("dust"))
                    unlockedBy("has_resin", has(STICKY_RESIN.get()))
                }
            }

            assembler {
                defaults {
                    workTicks(ASSEMBLY_TICKS)
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
                    workTicks(ASSEMBLY_TICKS)
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
                    input(WAFERS.getValue(key).get())
                    input("water", amount = 0.75)
                    voltage(Voltage.LV)
                    workTicks(300)
                }
            }
        }
    }
}
