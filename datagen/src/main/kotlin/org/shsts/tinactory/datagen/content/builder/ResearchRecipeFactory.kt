package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.core.recipe.ResearchRecipe
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

class ResearchRecipeBuilder(parent: IRecipeFactory<ResearchRecipe, ResearchRecipeBuilder>) :
    ProcessingRecipeBuilder<ResearchRecipe, ResearchRecipeBuilder>(parent) {
    private var target: ResourceLocation? = null
    private var progress = 1L

    fun target(value: ResourceLocation) {
        target = value
    }

    fun progress(value: Long) {
        progress = value
    }

    override fun validate() {
        super.validate()
        checkNotNull(target)
        check(progress > 0L)
    }

    override fun createObject(): ResearchRecipe {
        return ResearchRecipe(inputs, workTicks!!, voltage!!.value, power!!, target!!, progress)
    }
}

class ResearchRecipeFactory(recipeType: IRecipeType<ResearchRecipe>,
    defaults: ResearchRecipeBuilder.() -> Unit = {}) :
    RecipeFactory<ResearchRecipe, ResearchRecipeBuilder>(recipeType, ::ResearchRecipeBuilder, defaults) {
    fun target(loc: ResourceLocation, block: ResearchRecipeBuilder.() -> Unit) {
        recipe(loc) {
            target(loc)
            block()
        }
    }
}
