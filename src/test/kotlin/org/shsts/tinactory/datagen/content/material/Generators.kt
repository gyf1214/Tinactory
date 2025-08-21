package org.shsts.tinactory.datagen.content.material

import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.combustionGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.gasTurbine
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.steamTurbine

object Generators {
    fun init() {
        steamTurbine {
            generator("water", 0.08, 100, input = "gas", output = "liquid",
                voltages = Voltage.between(Voltage.ULV, Voltage.HV))
        }
        gasTurbine {
            generator("methane", 0.08, 100)
            generator("lpg", 0.32, 100)
            generator("refinery_gas", 0.064, 100)
            generator("natural_gas", 0.04, 100)
        }
        combustionGenerator {
            generator("ethanol", 0.16, 100)
            generator("diesel", 0.4, 125)
        }
    }

    private fun ProcessingRecipeFactory.generator(name: String, ratio: Double,
        ticks: Long, input: String = "fluid", output: String? = null,
        voltages: List<Voltage> = Voltage.between(Voltage.LV, Voltage.HV)) {
        for (v in voltages) {
            val decay = 1.4 - v.rank * 0.1
            val outputAmount = v.value * ticks / ratio
            val inputAmount = outputAmount * decay
            input(name, input, inputAmount, suffix = "_${v.id}") {
                output?.let { output(name, it, outputAmount) }
                voltage(v)
                workTicks(ticks)
            }
        }
    }
}
