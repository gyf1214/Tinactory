package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.core.recipe.AssemblyRecipe

class AssemblyRecipeBuilder(builder: AssemblyRecipe.Builder) :
    ProcessingRecipeBuilder<AssemblyRecipe.Builder>(builder) {
    fun tech(vararg loc: ResourceLocation) {
        builder.requireTech(*loc)
    }
}
