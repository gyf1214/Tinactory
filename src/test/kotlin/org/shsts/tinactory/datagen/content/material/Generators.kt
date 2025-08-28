package org.shsts.tinactory.datagen.content.material

import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.combustionGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.gasTurbine
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.steamTurbine

object Generators {
    fun init() {
        steamTurbine {
            generator("water", 80, 100, input = "gas", output = "liquid",
                voltages = Voltage.between(
                    Voltage.ULV, Voltage.HV))
        }
        gasTurbine {
            generator("methane", 80, 100)
            generator("lpg", 320, 100)
            generator("refinery_gas", 64, 100)
            generator("natural_gas", 40, 100)
        }
        combustionGenerator {
            generator("ethanol", 160, 100)
            generator("diesel", 400, 125)
        }
    }

    private fun ProcessingRecipeFactory.generator(name: String, ratio: Number,
        ticks: Long, input: String = "fluid", output: String? = null,
        voltages: List<Voltage> = Voltage.between(
            Voltage.LV, Voltage.HV)) {
        for (v in voltages) {
            val decay = 1.4 - v.rank * 0.1
            val outputAmount = v.value * ticks / (ratio.toDouble() * 1000)
            val inputAmount = outputAmount * decay
            input(name, input, inputAmount, suffix = "_${v.id}") {
                output?.let { output(name, it, outputAmount) }
                voltage(v)
                workTicks(ticks)
            }
        }
    }
}
