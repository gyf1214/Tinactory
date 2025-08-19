package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.material.MaterialSet
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.suffix
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

class RecipeFactory<B : ProcessingRecipe.BuilderBase<*, B>, RB : ProcessingRecipeBuilder<*>>(
    private val recipeType: IRecipeType<B>, private val factory: (B) -> RB,
    private val defaults: RB.() -> Unit = {}) {

    private var userDefaults: RB.() -> Unit = {}

    private fun apply(inner: B, block: RB.() -> Unit) {
        val ret = factory(inner)
        ret.defaults()
        ret.userDefaults()
        ret.block()
        ret.build()
    }

    fun recipe(loc: ResourceLocation, block: RB.() -> Unit) {
        apply(recipeType.recipe(DATA_GEN, loc), block)
    }

    private fun matLoc(mat: MaterialSet, sub: String, suffix: String): ResourceLocation {
        val loc = if (mat.hasFluid(sub)) mat.fluidLoc(sub) else mat.loc(sub)
        return suffix(loc, suffix)
    }

    fun defaults(value: RB.() -> Unit) {
        userDefaults = value
    }

    fun outputItem(item: ItemLike, suffix: String = "", amount: Int = 1,
        rate: Double = 1.0, block: RB.() -> Unit = {}) {
        recipe(suffix(item.asItem().registryName!!, suffix)) {
            outputItem({ item }, amount, rate = rate)
            block()
        }
    }

    fun outputMaterial(mat: MaterialSet, sub: String, amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: RB.() -> Unit = {}) {
        recipe(matLoc(mat, sub, suffix)) {
            outputMaterial(mat, sub, amount, rate = rate)
            block()
        }
    }

    fun outputMaterial(name: String, sub: String, amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: RB.() -> Unit = {}) {
        outputMaterial(getMaterial(name), sub, amount, suffix, rate, block)
    }
}
