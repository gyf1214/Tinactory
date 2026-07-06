package org.shsts.tinactory.datagen.content.builder

import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance.hasItems
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.hasTag
import org.shsts.tinactory.api.TinactoryKeys
import org.shsts.tinactory.core.util.LocHelper.suffix
import org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN
import org.shsts.tinactory.datagen.content.RegistryHelper.itemLoc

class VanillaRecipeFactory(private val replace: Boolean) {
    private fun build(item: ItemLike, suffix: String = "", block: () -> RecipeBuilder) {
        val loc = suffix(itemLoc(item), suffix)
        if (replace) {
            DATA_GEN.vanillaRecipe(loc, block)
        } else {
            val id1 = if (loc.namespace == TinactoryKeys.ID) {
                "craft/${loc.path}"
            } else {
                "craft/${loc.namespace}/${loc.path}"
            }
            DATA_GEN.vanillaRecipe(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, id1), block)
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
        suffix: String = "", category: RecipeCategory = RecipeCategory.MISC,
        criteria: String = "has_ingredient",
        block: ShapelessRecipeBuilder.() -> Unit = {}) {
        build(to, suffix) {
            ShapelessRecipeBuilder
                .shapeless(category, to, toAmount)
                .requires(Ingredient.of(from), fromAmount)
                .unlockedBy(criteria, hasTag(from))
                .also(block)
        }
    }

    fun smelting(from: TagKey<Item>, to: ItemLike, ticks: Int, suffix: String = "",
        category: RecipeCategory = RecipeCategory.MISC) {
        build(to, suffix) {
            SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(from), category, to, 0f, ticks)
                .unlockedBy("has_ingredient", hasTag(from))
        }
    }

    fun shaped(output: ItemLike, amount: Int = 1, suffix: String = "",
        category: RecipeCategory = RecipeCategory.MISC,
        block: ShapedRecipeBuilder.() -> Unit) {
        build(output, suffix) {
            ShapedRecipeBuilder.shaped(category, output, amount).apply(block)
        }
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, item: ItemLike) {
        unlockedBy(name, hasItems(item))
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, tag: TagKey<Item>) {
        unlockedBy(name, hasTag(tag))
    }

    fun ShapedRecipeBuilder.unlockedBy(name: String, mat: String, sub: String) {
        unlockedBy(name, getMaterial(mat).tag(sub))
    }

    fun ShapedRecipeBuilder.define(ch: Char, mat: String, sub: String) {
        define(ch, getMaterial(mat).tag(sub))
    }
}
