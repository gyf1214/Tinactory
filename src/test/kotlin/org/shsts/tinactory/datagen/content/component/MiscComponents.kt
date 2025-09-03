package org.shsts.tinactory.datagen.content.component

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllItems.ADVANCED_ALLOY
import org.shsts.tinactory.content.AllItems.BASIC_BUZZSAW
import org.shsts.tinactory.content.AllItems.GOOD_BUZZSAW
import org.shsts.tinactory.content.AllItems.GOOD_GRINDER
import org.shsts.tinactory.content.AllItems.ITEM_FILTER
import org.shsts.tinactory.content.AllItems.MIXED_METAL_INGOT
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.implosionCompressor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS

object MiscComponents {
    fun init() {
        ulv()

        // buzzsaw
        lathe {
            defaults {
                workTicks(240)
            }
            output(BASIC_BUZZSAW.get()) {
                input("cobalt_brass", "gear")
                voltage(Voltage.LV)
            }
            output(GOOD_BUZZSAW.get()) {
                input("vanadium_steel", "gear")
                voltage(Voltage.MV)
            }
            // TODO: advanced_buzzsaw
        }

        assembler {
            output(ITEM_FILTER.get()) {
                input("steel", "plate")
                input("zinc", "foil", 8)
                voltage(Voltage.LV)
                workTicks(200)
                tech(Technologies.SIFTING)
            }
            output(GOOD_GRINDER.get()) {
                input("diamond", "gem_flawless")
                input("steel", "plate", 8)
                input("diamond", "dust", 4)
                voltage(Voltage.MV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            // TODO: advanced_grinder

            output(MIXED_METAL_INGOT.get()) {
                input("aluminium", "plate")
                input("stainless_steel", "plate")
                input("chrome", "plate")
                input("soldering_alloy", amount = 2)
                voltage(Voltage.HV)
                workTicks(200)
                tech(Technologies.TNT)
            }
        }

        implosionCompressor {
            output(ADVANCED_ALLOY.get()) {
                input(MIXED_METAL_INGOT.get())
                input(Items.TNT, 24, port = 1)
                voltage(Voltage.HV)
            }
        }

        researches()
    }

    private fun ulv() {
        val cable = getComponent("cable").item(Voltage.ULV)

        vanilla {
            shapeless(getMaterial("iron").tag("wire"),
                cable, fromAmount = 4, criteria = "has_wire")
        }

        toolCrafting {
            result(getComponent("machine_hull").item(Voltage.ULV)) {
                pattern("###")
                pattern("#W#")
                pattern("###")
                define('#', "iron", "plate")
                define('W', cable)
                toolTag(TOOL_WRENCH)
            }
            result(getComponent("fluid_cell").item(Voltage.ULV)) {
                pattern("###")
                pattern("#G#")
                pattern(" # ")
                define('#', "iron", "plate")
                define('G', "glass", "primary")
                toolTag(TOOL_HAMMER, TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.SOLDERING)
            }
            component("fluid_cell") {
                input("iron", "plate", 4)
                input("glass", "primary")
                input("soldering_alloy")
            }
            output(Items.NAME_TAG) {
                input("iron", "plate")
                input(TOOL_HANDLE)
            }
        }
    }

    private fun researches() {
        research(Voltage.ULV) {
            input("iron", "plate")
            input("copper", "wire")
        }

        research(Voltage.LV) {
            input(getComponent("electric_motor").item(Voltage.LV))
            input("steel", "gear")
        }

        research(Voltage.MV) {
            input(getComponent("electric_pump").item(Voltage.MV))
            input(circuitBoard(CircuitTier.CPU).get())
        }
    }

    private fun research(voltage: Voltage, block: AssemblyRecipeBuilder.() -> Unit) {
        assembler {
            output(getComponent("research_equipment").item(voltage)) {
                voltage(voltage)
                workTicks(200)
                block()
            }
        }
    }
}
