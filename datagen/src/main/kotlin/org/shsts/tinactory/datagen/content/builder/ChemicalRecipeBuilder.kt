package org.shsts.tinactory.datagen.content.builder

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.api.logistics.PortType
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe

class ChemicalRecipeBuilder(builder: ChemicalReactorRecipe.Builder) :
    ProcessingRecipeBuilder<ChemicalReactorRecipe.Builder>(builder) {
    companion object {
        private val LOGGER = LogUtils.getLogger()
    }

    private var requireMultiBlockSet = false

    fun tech(vararg loc: ResourceLocation) {
        builder.requireTech(*loc)
    }

    fun requireMultiblock() {
        builder.requireMultiblock(true)
        requireMultiBlockSet = true
    }

    private fun needMultiblock(): Boolean {
        val inputs = builder.inputs
        val outputs = builder.outputs

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

    override fun build() {
        if (!requireMultiBlockSet && needMultiblock()) {
            LOGGER.trace("{} recipe need multiblock", builder.loc)
            builder.requireMultiblock(true)
        }
        super.build()
    }
}
