package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeType
import org.shsts.tinactory.content.recipe.MarkerRecipe
import org.shsts.tinactory.core.util.LocHelper.modLoc

class MarkerBuilder(builder: MarkerRecipe.Builder, var baseType: String? = null) :
    ProcessingRecipeBuilder<MarkerRecipe.Builder>(builder) {
    init {
        requirePower = false
    }

    fun baseType(id: String) {
        baseType = id
        builder.baseType(modLoc(id))
    }

    fun baseType(type: RecipeType<*>) {
        builder.baseType(ResourceLocation(type.toString()))
    }

    fun prefix(value: String) {
        if (baseType != null) {
            builder.prefix("$baseType/$value")
        } else {
            builder.prefix(value)
        }
    }

    fun prefix() {
        builder.prefix(baseType!!).requireMultiblock(true)
    }
}
