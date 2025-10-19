package org.shsts.tinactory.datagen.content.component

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllItems.STORAGE_CELLS
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ResearchRecipe
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactoryBase
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.implosionCompressor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.rocket
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.wiremill
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
            misc("buzzsaw/basic") {
                input("cobalt_brass", "gear")
                voltage(Voltage.LV)
            }
            misc("buzzsaw/good") {
                input("vanadium_steel", "gear")
                voltage(Voltage.MV)
            }
            misc("buzzsaw/advanced") {
                input("tungsten_carbide", "gear")
                voltage(Voltage.HV)
            }
        }

        assembler {
            misc("item_filter") {
                input("steel", "plate")
                input("zinc", "foil", 8)
                voltage(Voltage.LV)
                workTicks(200)
                tech(Technologies.SIFTING)
            }
            misc("grinder/good") {
                input("diamond", "gem_flawless")
                input("steel", "plate", 8)
                input("diamond", "dust", 4)
                voltage(Voltage.MV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            misc("grinder/advanced") {
                input("tungsten_carbide", "gear")
                input("tungsten_steel", "plate", 8)
                input("tungsten_carbide", "dust", 4)
                voltage(Voltage.HV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            misc("mixed_metal_ingot") {
                input("aluminium", "plate")
                input("stainless_steel", "plate")
                input("chrome", "plate")
                input("soldering_alloy", amount = 2)
                voltage(Voltage.HV)
                workTicks(200)
                tech(Technologies.TNT)
            }
            misc("carbon_mesh") {
                misc("carbon_fiber", 4)
                input("epoxy", amount = 2)
                voltage(Voltage.HV)
                workTicks(256)
                tech(Technologies.CARBON_FIBER)
            }
        }

        wiremill {
            misc("carbon_fiber") {
                input("carbon", "ingot", 4)
                workTicks(192)
                voltage(Voltage.HV)
            }
        }

        implosionCompressor {
            defaults {
                voltage(Voltage.HV)
            }
            misc("advanced_alloy") {
                misc("mixed_metal_ingot")
                input(Items.TNT, 12, port = 1)
            }
            output("carbon", "ingot") {
                input("carbon", "dust", 4)
                input(Items.TNT, 2, port = 1)
            }
            misc("carbon_plate") {
                misc("carbon_mesh")
                input(Items.TNT, 12, port = 1)
            }
        }

        researches()
        ae()
        rockets()
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>, RB : ProcessingRecipeBuilder<*>> RecipeFactory<B, RB>.misc(
        id: String, amount: Int = 1, block: RB.() -> Unit) {
        output(getItem("component/$id"), amount, block = block)
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>> ProcessingRecipeBuilder<B>.misc(
        id: String, amount: Int = 1) {
        input(getItem("component/$id"), amount)
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
            component("machine_hull") {
                input("iron", "plate", 8)
                component("cable")
            }
            component("cable") {
                input("iron", "wire", 4)
            }
            component("fluid_cell") {
                input("iron", "plate", 4)
                input("glass", "primary")
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

        research(Voltage.HV) {
            input(getComponent("conveyor_module").item(Voltage.HV))
            input(getItem("component/advanced_alloy"))
            input("pvc", "molten", 2)
        }

        research(Voltage.EV) {
            input(getComponent("robot_arm").item(Voltage.EV))
            input(getItem("component/carbon_plate"))
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

    private fun ae() {
        val annihilation = getItem("component/annihilation_core")
        val formation = getItem("component/formation_core")

        assembler {
            defaults {
                voltage(Voltage.HV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.DIGITAL_STORAGE)
            }
            output(annihilation, 2) {
                circuit(1, Voltage.MV)
                component("robot_arm", 1, Voltage.MV)
                input("nether_quartz", "primary", 4)
                input("fluix", "dust", 4)
                input("annealed_copper", "wire_fine", 8)
                input("pvc")
            }
            output(formation, 2) {
                circuit(1, Voltage.MV)
                component("conveyor_module", 1, Voltage.MV)
                input("certus_quartz", "gem", 4)
                input("fluix", "dust", 4)
                input("annealed_copper", "wire_fine", 8)
                input("pvc")
            }

            for ((i, entry) in STORAGE_CELLS.withIndex()) {
                val component = entry.component.get()
                output(component) {
                    circuit(1, Voltage.fromRank(i + 2))
                    if (i > 0) {
                        input(STORAGE_CELLS[i - 1].component.get(), 3)
                    }
                    when (i) {
                        0 -> input(CHIP.item("ram"), 4)
                        1 -> input(CHIP.item("nor"), 4)
                        2 -> input(CHIP.item("nor"), 8)
                        3 -> input(CHIP.item("nand"), 4)
                    }
                    if (i < 2) {
                        input("certus_quartz", "gem", 4)
                    } else {
                        input("fluix", "gem", 4)
                    }
                    val wireMat = when (i) {
                        0 -> "annealed_copper"
                        1 -> "platinum"
                        // TODO
                        2 -> "platinum"
                        3 -> "platinum"
                        else -> throw IllegalStateException()
                    }
                    input(wireMat, "wire_fine", 16)
                    input("pvc")
                }
                output(entry.item.get()) {
                    input(component)
                    input(annihilation)
                    input(formation)
                    input("aluminium", "plate", 3)
                    input("soldering_alloy", amount = 3)
                }
                output(entry.fluid.get()) {
                    input(component)
                    input(annihilation)
                    input(formation)
                    input("stainless_steel", "plate", 3)
                    input("soldering_alloy", amount = 3)
                }
            }
        }
    }

    private fun rockets() {
        rocket {
            rocket(Technologies.ROCKET_T1) {
                input(AllTags.circuit(Voltage.EV))
                input(getComponent("electric_pump").item(Voltage.HV), 4)
                input(getItem("component/advanced_alloy"), 16)
                input("cetane_boosted_diesel")
                voltage(Voltage.HV)
            }
        }
    }

    private fun ProcessingRecipeFactoryBase<ResearchRecipe.Builder>.rocket(
        loc: ResourceLocation, block: ProcessingRecipeBuilder<ResearchRecipe.Builder>.() -> Unit) {
        recipe(loc) {
            extra {
                target(loc)
            }
            block()
        }
    }
}
