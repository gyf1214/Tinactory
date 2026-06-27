package org.shsts.tinactory.datagen.content.builder

import com.mojang.logging.LogUtils
import org.shsts.tinactory.api.logistics.PortType
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

class ChemicalRecipeBuilder(parent: IRecipeFactory<ChemicalReactorRecipe, ChemicalRecipeBuilder>) :
    AssemblyRecipeBuilder<ChemicalReactorRecipe, ChemicalRecipeBuilder>(
        parent,
        { inputs, outputs, workTicks, voltage, power, requiredTech ->
            ChemicalReactorRecipe(inputs, outputs, workTicks, voltage, power, requiredTech, false)
        }) {
    companion object {
        private val LOGGER = LogUtils.getLogger()
    }

    private var requireMultiBlockSet = false
    private var requireMultiblock = false

    fun requireMultiblock() {
        requireMultiblock = true
        requireMultiBlockSet = true
    }

    private fun needMultiblock(): Boolean {
        var itemInputs = 0
        var fluidInputs = 0
        var itemOutputs = 0
        var fluidOutputs = 0

        for (input in inputs) {
            if (input.ingredient.type() == PortType.ITEM) {
                itemInputs++
            } else if (input.ingredient.type() == PortType.FLUID) {
                fluidInputs++
            }
        }

        for (output in outputs) {
            if (output.result.type() == PortType.ITEM) {
                itemOutputs++
            } else if (output.result.type() == PortType.FLUID) {
                fluidOutputs++
            }
        }

        return itemInputs > 2 || fluidInputs > 2 || itemOutputs > 2 || fluidOutputs > 2
    }

    override fun createObject(): ChemicalReactorRecipe {
        return ChemicalReactorRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            requiredTech.toList(), requireMultiblock)
    }

    override fun build(): IRecipeFactory<ChemicalReactorRecipe, ChemicalRecipeBuilder> {
        if (!requireMultiBlockSet && needMultiblock()) {
            LOGGER.trace("chemical reactor recipe needs multiblock")
            requireMultiblock = true
        }
        return super.build()
    }
}
