package org.shsts.tinactory.datagen.content

import net.minecraft.world.item.crafting.RecipeType
import org.shsts.tinactory.content.AllRecipes.PROCESSING_TYPES
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.MarkerFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.marker
import org.shsts.tinactory.integration.jei.category.RecipeCategory

object Markers {
    fun init() {
        marker {
            // base markers, i.e. marking all recipes in baseType for multi-use multiblocks
            recipe("smelting") {
                baseType(RecipeType.SMELTING)
                extra {
                    requireMultiblock(true)
                }
            }
            baseMarker("alloy_smelter")
            baseMarker("arc_furnace")
            baseMarker("bender")
            baseMarker("wiremill")

            for (variant in OreVariant.entries) {
                val name = variant.serializedName
                recipe("analyze_$name") {
                    baseType("ore_analyzer")
                    prefix(name)
                    input(variant.baseItem, port = 0)
                    voltage(variant.voltage)
                }
            }
        }

        trackJEICategory()
    }

    private fun MarkerFactory.baseMarker(id: String) {
        recipe(id) {
            baseType(id)
            prefix()
        }
    }

    private fun trackJEICategory() {
        val allTypes = buildList {
            add(modLoc("tool_crafting"))
            for (loc in PROCESSING_TYPES.keys) {
                add(loc)
            }
        }

        dataGen {
            for (type in allTypes) {
                trackLang(RecipeCategory.categoryTitleId(type))
            }
        }
    }
}
