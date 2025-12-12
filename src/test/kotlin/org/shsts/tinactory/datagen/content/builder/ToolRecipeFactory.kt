package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllRecipes.TOOL_CRAFTING
import org.shsts.tinactory.content.recipe.ToolRecipe
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

class ToolRecipeFactory {
    fun recipe(loc: ResourceLocation, block: ToolRecipe.Builder.() -> Unit) {
        TOOL_CRAFTING.recipe(DATA_GEN, loc).apply {
            block()
            build()
        }
    }

    fun result(item: ItemLike, amount: Int = 1, block: ToolRecipe.Builder.() -> Unit) {
        recipe(item.asItem().registryName!!) {
            result({ item }, amount)
            block()
        }
    }

    fun result(name: String, sub: String, amount: Int = 1, block: ToolRecipe.Builder.() -> Unit) {
        val mat = getMaterial(name)
        recipe(mat.loc(sub)) {
            result(mat.entry(sub), amount)
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

    fun ToolRecipe.Builder.define(ch: Char, mat: String, sub: String) {
        define(ch, getMaterial(mat).tag(sub))
    }
}
