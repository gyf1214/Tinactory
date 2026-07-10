package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.core.util.LocHelper.suffix
import org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN
import org.shsts.tinactory.datagen.content.RegistryHelper.itemLoc
import org.shsts.tinactory.datagen.content.RegistryHelper.recipeLoc
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

open class RecipeFactory<R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>>(
    val recipeType: IRecipeType<R>,
    builderFactory: (IRecipeFactory<R, B>) -> B,
    private val defaults: B.() -> Unit = {}) {
    private val factory = DATA_GEN.recipeFactory(recipeType, builderFactory)
    private var userDefaults: B.() -> Unit = {}
    var defaultItemSub: String? = null
    var defaultFluidSub = "fluid"

    private fun defaultSub(mat: MaterialSet) =
        defaultItemSub?.takeIf { mat.hasItem(it) } ?: defaultFluidSub

    private fun defaultSub(name: String) = defaultSub(getMaterial(name))

    private fun matLoc(mat: MaterialSet, sub: String, suffix: String = ""): ResourceLocation {
        val loc = if (mat.hasFluid(sub)) mat.fluidLoc(sub) else mat.loc(sub)
        return suffix(loc, suffix)
    }

    protected open fun classDefaults(builder: B) {
        builder.defaultItemSub = this@RecipeFactory.defaultItemSub
        builder.defaultFluidSub = this@RecipeFactory.defaultFluidSub
    }

    protected open fun onBuild(loc: ResourceLocation, builder: B) {}

    fun recipe(loc: ResourceLocation, block: B.() -> Unit) {
        val recipeId = recipeLoc(recipeType, loc)
        factory.recipe(recipeId).apply {
            defaults()
            classDefaults(this)
            userDefaults()
            block()
            onBuild(recipeId, this)
            build()
        }
    }

    fun recipe(id: String, block: B.() -> Unit) {
        recipe(modLoc(id), block)
    }

    fun recipe(mat: MaterialSet, sub: String = defaultSub(mat),
        suffix: String = "", block: B.() -> Unit) {
        recipe(matLoc(mat, sub, suffix), block)
    }

    fun defaults(value: B.() -> Unit) {
        userDefaults = value
    }

    fun input(tag: TagKey<Item>, amount: Int = 1,
        suffix: String = "", block: B.() -> Unit = {}) {
        recipe(suffix(tag.location, suffix)) {
            input(tag, amount)
            block()
        }
    }

    fun input(item: ItemLike, amount: Int = 1,
        suffix: String = "", block: B.() -> Unit = {}) {
        recipe(suffix(itemLoc(item), suffix)) {
            input(item, amount)
            block()
        }
    }

    fun input(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1,
        suffix: String = "", block: B.() -> Unit = {}) {
        recipe(matLoc(mat, sub, suffix)) {
            input(mat, sub, amount)
            block()
        }
    }

    fun input(name: String, sub: String = defaultSub(name), amount: Number = 1,
        suffix: String = "", block: B.() -> Unit = {}) {
        input(getMaterial(name), sub, amount, suffix, block)
    }

    fun output(item: ItemLike, amount: Int = 1, suffix: String = "",
        rate: Double = 1.0, block: B.() -> Unit = {}) {
        recipe(suffix(itemLoc(item), suffix)) {
            output(item, amount, rate = rate)
            block()
        }
    }

    fun output(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: B.() -> Unit = {}) {
        recipe(matLoc(mat, sub, suffix)) {
            output(mat, sub, amount, rate = rate)
            block()
        }
    }

    fun output(name: String, sub: String = defaultSub(name), amount: Number = 1,
        suffix: String = "", rate: Double = 1.0, block: B.() -> Unit = {}) {
        output(getMaterial(name), sub, amount, suffix, rate, block)
    }
}
