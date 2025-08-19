package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllMaterials
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.test.TinactoryTest
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

class RecipeFactory<B : ProcessingRecipe.BuilderBase<*, B>, RB : ProcessingRecipeBuilder<*>>(
    private val recipeType: IRecipeType<B>, private val factory: (B) -> RB,
    private val defaults: RB.() -> Unit = {}) {

    private var userDefaults: RB.() -> Unit = {}

    private fun createBuilder(inner: B): RB {
        val ret = factory(inner)
        ret.defaults()
        ret.userDefaults()
        return ret
    }

    fun recipe(loc: ResourceLocation, block: RB.() -> Unit) {
        val builder = createBuilder(recipeType.recipe(TinactoryTest.DATA_GEN, loc))
        builder.block()
        builder.build()
    }

    private fun matLoc(name: String, sub: String): ResourceLocation {
        val mat = AllMaterials.getMaterial(name)
        return if (mat.hasFluid(sub)) mat.fluidLoc(sub) else mat.loc(sub)
    }

    fun defaults(value: RB.() -> Unit) {
        userDefaults = value
    }

    fun outputItem(item: ItemLike, amount: Int = 1, rate: Double = 1.0, block: RB.() -> Unit = {}) {
        recipe(item.asItem().registryName!!) {
            outputItem({ item }, amount, rate = rate)
            block()
        }
    }

    fun outputMaterial(name: String, sub: String, amount: Number = 1, block: RB.() -> Unit = {}) {
        recipe(matLoc(name, sub)) {
            outputMaterial(name, sub, amount)
            block()
        }
    }
}
