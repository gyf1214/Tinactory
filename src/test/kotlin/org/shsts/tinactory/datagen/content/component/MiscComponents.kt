package org.shsts.tinactory.datagen.content.component

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR
import org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP
import org.shsts.tinactory.content.AllItems.FLUID_CELL
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.RegistryHelper.modItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactory
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS

object MiscComponents {
    fun init() {
        ulv()

        // buzzsaw
        lathe {
            defaults {
                workTicks(240)
            }
            componentItem("buzzsaw/basic") {
                input("cobalt_brass", "gear")
                voltage(Voltage.LV)
            }
            componentItem("buzzsaw/basic") {
                input("vanadium_steel", "gear")
                voltage(Voltage.MV)
            }
            // TODO: advanced_buzzsaw
        }

        assembler {
            componentItem("item_filter") {
                input("steel", "plate")
                input("zinc", "foil", 8)
                voltage(Voltage.LV)
                workTicks(200)
                tech(Technologies.SIFTING)
            }
            componentItem("grinder/basic") {
                input("diamond", "gem_flawless")
                input("steel", "plate", 8)
                input("diamond", "dust", 4)
                voltage(Voltage.MV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            // TODO: advanced_grinder
        }

        research(Voltage.ULV) {
            input("iron", "plate")
            input("copper", "wire")
        }

        research(Voltage.LV) {
            input(ELECTRIC_MOTOR.item(Voltage.LV))
            input("steel", "gear")
        }

        research(Voltage.MV) {
            input(ELECTRIC_PUMP.item(Voltage.MV))
            input(circuitBoard(CircuitTier.CPU).get())
        }
    }

    private fun ulv() {
        vanilla {
            shapeless(getMaterial("iron").tag("wire"),
                CABLE.item(Voltage.ULV),
                fromAmount = 4, criteria = "has_wire")
        }

        toolCrafting {
            result(MACHINE_HULL.item(Voltage.ULV)) {
                pattern("###")
                pattern("#W#")
                pattern("###")
                define('#', "iron", "plate")
                define('W', CABLE.item(Voltage.ULV))
                toolTag(TOOL_WRENCH)
            }
            result(FLUID_CELL.item(Voltage.ULV)) {
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
            output(FLUID_CELL) {
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

    private fun research(voltage: Voltage, block: AssemblyRecipeBuilder.() -> Unit) {
        assembler {
            output(RESEARCH_EQUIPMENT.item(voltage)) {
                voltage(voltage)
                workTicks(200)
                block()
            }
        }
    }

    private fun <RB : ProcessingRecipeBuilder<*>> RecipeFactory<*, RB>.componentItem(
        id: String, block: RB.() -> Unit) {
        output(modItem("component/$id"), block = block)
    }
}
