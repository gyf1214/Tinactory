package org.shsts.tinactory.datagen.content

import org.shsts.tinactory.content.AllRecipes.PROCESSING_TYPES
import org.shsts.tinactory.content.AllTags.material
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.MarkerFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.marker
import org.shsts.tinactory.integration.jei.category.RecipeCategory

object Markers {
    fun init() {
        marker {
            crush("raw")
            crush("crushed")
            crush("crushed_purified")
            crush("crushed_centrifuged")
            wash("crushed")
            wash("dust_impure")
            wash("dust_pure")

            recipe("centrifuge_dust_pure") {
                baseType("centrifuge")
                prefix("material/dust_pure")
                input(material("dust_pure"), port = 0)
            }
            recipe("thermal_centrifuge_crushed_purified") {
                baseType("thermal_centrifuge")
                prefix("material/crushed_purified")
                input(material("crushed_purified"), port = 0)
            }

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

    private fun MarkerFactory.crush(sub: String) {
        recipe("crush_$sub") {
            baseType("macerator")
            prefix("material/$sub")
            input(material(sub), port = 0)
        }
    }

    private fun MarkerFactory.wash(sub: String) {
        recipe("wash_$sub") {
            baseType("ore_washer")
            prefix("material/$sub")
            input(material(sub), port = 0)
            input("water", port = 1)
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
