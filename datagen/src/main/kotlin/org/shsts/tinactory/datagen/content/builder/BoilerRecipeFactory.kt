package org.shsts.tinactory.datagen.content.builder

import net.neoforged.neoforge.fluids.FluidStack
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.BOILER
import org.shsts.tinactory.content.recipe.BoilerRecipe
import org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

class BoilerRecipeFactory {
    private val factory = DATA_GEN.recipeFactory(BOILER, ::BoilerRecipeBuilder)

    fun recipe(name: String, block: BoilerRecipeBuilder.() -> Unit) {
        val mat = getMaterial(name)
        factory.recipe(mat.fluidLoc("liquid"))
            .input(FluidStack(mat.fluid("liquid").get(), 1))
            .output(FluidStack(mat.fluid("gas").get(), 1))
            .apply(block)
            .build()
    }
}

class BoilerRecipeBuilder(parent: IRecipeFactory<BoilerRecipe, BoilerRecipeBuilder>) :
    Builder<BoilerRecipe, IRecipeFactory<BoilerRecipe, BoilerRecipeBuilder>, BoilerRecipeBuilder>(parent) {
    private lateinit var input: FluidStack
    private lateinit var output: FluidStack
    private var minHeat = 0.0
    private var optimalHeat = 0.0
    private var maxHeat = 0.0
    private var reactionRate = 0.0
    private var absorbRate = 0.0

    fun input(value: FluidStack): BoilerRecipeBuilder {
        input = value
        return this
    }

    fun output(value: FluidStack): BoilerRecipeBuilder {
        output = value
        return this
    }

    fun heat(minHeat: Double, optimalHeat: Double, maxHeat: Double): BoilerRecipeBuilder {
        this.minHeat = minHeat
        this.optimalHeat = optimalHeat
        this.maxHeat = maxHeat
        return this
    }

    fun reaction(reactionRate: Double, absorbRate: Double): BoilerRecipeBuilder {
        this.reactionRate = reactionRate
        this.absorbRate = absorbRate
        return this
    }

    override fun createObject(): BoilerRecipe {
        return BoilerRecipe(input, output, minHeat, optimalHeat, maxHeat, reactionRate, absorbRate)
    }
}
