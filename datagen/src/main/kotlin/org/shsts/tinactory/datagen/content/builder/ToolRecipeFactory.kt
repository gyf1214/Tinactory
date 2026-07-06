package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.ShapedRecipePattern
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.TOOL_CRAFTING
import org.shsts.tinactory.content.recipe.ToolRecipe
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN
import org.shsts.tinactory.datagen.content.RegistryHelper.itemLoc
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory
import java.util.function.Supplier

class ToolRecipeFactory {
    private val factory = DATA_GEN.recipeFactory(TOOL_CRAFTING, ::ToolRecipeBuilder)

    fun recipe(loc: ResourceLocation, block: ToolRecipeBuilder.() -> Unit) {
        factory.recipe(loc).apply {
            block()
            build()
        }
    }

    fun result(item: ItemLike, amount: Int = 1, block: ToolRecipeBuilder.() -> Unit) {
        recipe(itemLoc(item)) {
            result(item, amount)
            block()
        }
    }

    fun result(name: String, sub: String, amount: Int = 1, block: ToolRecipeBuilder.() -> Unit) {
        val mat = getMaterial(name)
        recipe(mat.loc(sub)) {
            result(mat.item(sub), amount)
            block()
        }
    }

    fun shapeless(from: ItemLike, to: ItemLike, tool: TagKey<Item>, amount: Int = 1) {
        result(to, amount) {
            pattern("#")
            define('#') { from }
            toolTag(tool)
        }
    }

    fun shapeless(from: TagKey<Item>, to: ItemLike, tool: TagKey<Item>, amount: Int = 1) {
        result(to, amount) {
            pattern("#")
            define('#', from)
            toolTag(tool)
        }
    }

    fun ToolRecipeBuilder.define(ch: Char, mat: String, sub: String) {
        define(ch, getMaterial(mat).tag(sub))
    }
}

class ToolRecipeBuilder(parent: IRecipeFactory<ToolRecipe, ToolRecipeBuilder>) :
    Builder<ToolRecipe, IRecipeFactory<ToolRecipe, ToolRecipeBuilder>, ToolRecipeBuilder>(parent) {
    private var result: Supplier<out ItemLike>? = null
    private var count = 0
    private val rows = mutableListOf<String>()
    private val keys = mutableMapOf<Char, Supplier<Ingredient>>()
    private val tools = mutableListOf<Supplier<Ingredient>>()

    fun result(result: ItemLike, count: Int): ToolRecipeBuilder {
        this.result = Supplier { result }
        this.count = count
        return this
    }

    fun pattern(row: String): ToolRecipeBuilder {
        rows += row
        return this
    }

    fun define(key: Char, item: Supplier<out ItemLike>): ToolRecipeBuilder {
        keys[key] = Supplier { Ingredient.of(item.get()) }
        return this
    }

    fun define(key: Char, tag: TagKey<Item>): ToolRecipeBuilder {
        keys[key] = Supplier { Ingredient.of(tag) }
        return this
    }

    fun define(key: Char, item: Item): ToolRecipeBuilder {
        keys[key] = Supplier { Ingredient.of(item) }
        return this
    }

    fun tool(ingredient: Supplier<Ingredient>): ToolRecipeBuilder {
        tools += ingredient
        return this
    }

    fun toolTag(vararg toolTags: TagKey<Item>): ToolRecipeBuilder {
        for (tag in toolTags) {
            tool { Ingredient.of(tag) }
        }
        return this
    }

    override fun createObject(): ToolRecipe {
        val resultStack = ItemStack(checkNotNull(result) { "Missing tool recipe result" }.get(), count)
        val pattern = ShapedRecipePattern.of(keys.mapValues { it.value.get() }, rows)
        val shaped = ShapedRecipe("", CraftingBookCategory.MISC, pattern, resultStack)
        return ToolRecipe(shaped, tools.map { it.get() })
    }
}
