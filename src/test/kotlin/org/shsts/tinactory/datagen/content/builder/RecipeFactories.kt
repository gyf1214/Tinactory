package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory
import org.shsts.tinactory.core.recipe.ProcessingRecipe

typealias ProcessingRecipeFactory = RecipeFactory<ProcessingRecipe.Builder,
    ProcessingRecipeBuilder<ProcessingRecipe.Builder>>

object RecipeFactories {
    private fun <B : ProcessingRecipe.BuilderBase<*, B>> processing(name: String,
        defaults: ProcessingRecipeBuilder<B>.() -> Unit = {}):
        RecipeFactory<B, ProcessingRecipeBuilder<B>> {

        val recipeType = Tinactory.REGISTRATE.getRecipeType<B>(name)
        return RecipeFactory(recipeType, ::ProcessingRecipeBuilder, defaults)
    }

    private fun simpleProcessing(name: String,
        defaults: ProcessingRecipeBuilder<ProcessingRecipe.Builder>.() -> Unit = {}) =
        processing(name, defaults)

    fun vacuumFreezer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("vacuum_freezer") {
            fullDefaults()
            amperage = 1.5
        }.block()
    }

    fun stoneGenerator(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("stone_generator") {
            defaultOutputItem = 0
            defaultOutputFluid = 1
            amperage = 0.125
            workTicks(20)
        }.block()
    }
}
