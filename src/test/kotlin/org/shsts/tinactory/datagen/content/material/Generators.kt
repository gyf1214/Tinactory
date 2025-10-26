package org.shsts.tinactory.datagen.content.material

import org.shsts.tinactory.content.recipe.GeneratorRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactoryBase
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.combustionGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.gasTurbine
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.steamTurbine

object Generators {
    fun init() {
        steamTurbine {
            generator("water", 80, 100, input = "gas", output = "liquid",
                minVoltage = Voltage.ULV)

            input("coolant", "gas", 0.08) {
                output("water", "gas", 0.2)
                voltage(Voltage.HV)
                workTicks(125)
            }
        }
        gasTurbine {
            generator("natural_gas", 40, 100)
            generator("refinery_gas", 64, 100)
            generator("methane", 80, 100)
            generator("lpg", 400, 125)
            generator("rocket_fuel", 800, 125, minVoltage = Voltage.MV)
        }
        combustionGenerator {
            generator("ethanol", 160, 100)
            generator("diesel", 400, 125)
            generator("cetane_boosted_diesel", 800, 125, minVoltage = Voltage.MV)
        }
    }

    private fun ProcessingRecipeFactoryBase<GeneratorRecipe.Builder>.generator(
        name: String, ratio: Number, ticks: Long,
        input: String = "fluid", output: String? = null,
        minVoltage: Voltage = Voltage.LV) {
        for (v in Voltage.between(minVoltage, Voltage.HV)) {
            val decay = 1.4 - v.rank * 0.1
            val outputAmount = v.value * ticks / (ratio.toDouble() * 1000)
            val inputAmount = outputAmount * decay
            input(name, input, inputAmount, suffix = "_${v.id}") {
                output?.let { output(name, it, outputAmount) }
                voltage(v)
                workTicks(ticks)
                extra {
                    exactVoltage(v != Voltage.HV)
                }
            }
        }
    }
}
