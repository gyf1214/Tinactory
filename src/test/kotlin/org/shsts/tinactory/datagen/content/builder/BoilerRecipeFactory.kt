package org.shsts.tinactory.datagen.content.builder

import net.minecraftforge.fluids.FluidStack
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllRecipes.BOILER
import org.shsts.tinactory.content.recipe.BoilerRecipe
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

class BoilerRecipeFactory {
    fun recipe(name: String, block: BoilerRecipe.Builder.() -> Unit) {
        val mat = getMaterial(name)
        BOILER.recipe(DATA_GEN, mat.fluidLoc("liquid"))
            .input(FluidStack(mat.fluid("liquid").get(), 1))
            .output(FluidStack(mat.fluid("gas").get(), 1))
            .apply(block)
            .build()
    }
}
