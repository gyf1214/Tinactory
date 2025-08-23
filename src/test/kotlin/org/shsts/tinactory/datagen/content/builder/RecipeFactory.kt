package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
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
    var defaultItemSub: String? = null
    var defaultFluidSub = "fluid"

    private fun defaultSub(mat: MaterialSet) =
        defaultItemSub?.takeIf { mat.hasItem(it) } ?: defaultFluidSub

    private fun defaultSub(name: String) = defaultSub(getMaterial(name))

    private fun matLoc(mat: MaterialSet, sub: String, suffix: String = ""): ResourceLocation {
        val loc = if (mat.hasFluid(sub)) mat.fluidLoc(sub) else mat.loc(sub)
        return suffix(loc, suffix)
    }

    private fun apply(inner: B, block: RB.() -> Unit) {
        factory(inner).apply {
            defaults()
            defaultItemSub = this@RecipeFactory.defaultItemSub
            defaultFluidSub = this@RecipeFactory.defaultFluidSub
            userDefaults()
            block()
            build()
        }
    }

    fun recipe(loc: ResourceLocation, block: RB.() -> Unit) {
        apply(recipeType.recipe(DATA_GEN, loc), block)
    }

    fun recipe(mat: MaterialSet, sub: String = defaultSub(mat),
        suffix: String = "", block: RB.() -> Unit) {
        recipe(matLoc(mat, sub, suffix), block)
    }

    fun defaults(value: RB.() -> Unit) {
        userDefaults = value
    }

    fun input(tag: TagKey<Item>, amount: Int = 1,
        suffix: String = "", block: RB.() -> Unit = {}) {
        recipe(suffix(tag.location, suffix)) {
            input(tag, amount)
            block()
        }
    }

    fun input(item: ItemLike, amount: Int = 1,
        suffix: String = "", block: RB.() -> Unit = {}) {
        recipe(suffix(item.asItem().registryName!!, suffix)) {
            input(item, amount)
            block()
        }
    }

    fun input(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1,
        suffix: String = "", block: RB.() -> Unit = {}) {
        recipe(matLoc(mat, sub, suffix)) {
            input(mat, sub, amount)
            block()
        }
    }

    fun input(name: String, sub: String = defaultSub(name), amount: Number = 1,
        suffix: String = "", block: RB.() -> Unit = {}) {
        input(getMaterial(name), sub, amount, suffix, block)
    }

    fun output(item: ItemLike, amount: Int = 1, suffix: String = "",
        rate: Double = 1.0, block: RB.() -> Unit = {}) {
        recipe(suffix(item.asItem().registryName!!, suffix)) {
            output(item, amount, rate = rate)
            block()
        }
    }

    fun output(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: RB.() -> Unit = {}) {
        recipe(matLoc(mat, sub, suffix)) {
            output(mat, sub, amount, rate = rate)
            block()
        }
    }

    fun output(name: String, sub: String = defaultSub(name), amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: RB.() -> Unit = {}) {
        output(getMaterial(name), sub, amount, suffix, rate, block)
    }
}
