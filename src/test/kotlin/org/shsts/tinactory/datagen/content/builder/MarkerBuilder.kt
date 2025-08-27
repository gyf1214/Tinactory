package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.recipe.MarkerRecipe
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase

class MarkerBuilder(builder: MarkerRecipe.Builder) :
    ProcessingRecipeBuilder<MarkerRecipe.Builder>(builder) {
    init {
        requirePower = false
    }

    fun baseType(name: String) {
        builder.baseType(REGISTRATE.getRecipeType<IRecipeBuilderBase<*>>(name))
    }
}
