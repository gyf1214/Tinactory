package org.shsts.tinactory.datagen.content.builder

import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.has
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

class VanillaRecipeFactory(private val replace: Boolean) {
    private fun build(suffix: String = "", block: () -> RecipeBuilder) {
        if (replace) {
            DATA_GEN.replaceVanillaRecipe(block)
        } else {
            DATA_GEN.vanillaRecipe(block, suffix)
        }
    }

    fun nullRecipe(vararg args: Any) {
        for (arg in args) {
            when (arg) {
                is String -> DATA_GEN.nullRecipe(arg)
                is ResourceLocation -> DATA_GEN.nullRecipe(arg)
                is ItemLike -> DATA_GEN.nullRecipe(arg.asItem())
            }
        }
    }

    fun shapeless(from: TagKey<Item>, to: ItemLike, fromAmount: Int = 1, toAmount: Int = 1,
        suffix: String = "", criteria: String = "has_ingredient",
        block: ShapelessRecipeBuilder.() -> Unit = {}) {
        build(suffix) {
            ShapelessRecipeBuilder
                .shapeless(to, toAmount)
                .requires(Ingredient.of(from), fromAmount)
                .unlockedBy(criteria, has(from))
                .also(block)
        }
    }

    fun smelting(from: TagKey<Item>, to: ItemLike, ticks: Int, suffix: String = "") {
        build(suffix) {
            SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(from), to, 0f, ticks)
                .unlockedBy("has_ingredient", has(from))
        }
    }

    fun shaped(output: ItemLike, amount: Int = 1, suffix: String = "",
        block: ShapedRecipeBuilder.() -> Unit) {
        build(suffix) {
            ShapedRecipeBuilder.shaped(output, amount).apply(block)
        }
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, item: ItemLike) {
        unlockedBy(name, has(item))
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, tag: TagKey<Item>) {
        unlockedBy(name, has(tag))
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, mat: String, sub: String) {
        unlockedBy(name, getMaterial(mat).tag(sub))
    }

    fun ShapedRecipeBuilder.define(ch: Char, mat: String, sub: String) {
        define(ch, getMaterial(mat).tag(sub))
    }
}
