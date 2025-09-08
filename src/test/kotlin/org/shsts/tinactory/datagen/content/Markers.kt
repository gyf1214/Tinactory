package org.shsts.tinactory.datagen.content

import org.shsts.tinactory.content.AllBlockEntities.PROCESSING_SETS
import org.shsts.tinactory.content.AllMultiblocks.MULTIBLOCK_SETS
import org.shsts.tinactory.content.AllTags.material
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.marker
import org.shsts.tinactory.integration.jei.category.RecipeCategory

object Markers {
    fun init() {
        crush("raw")
        crush("crushed")
        crush("crushed_purified")
        crush("crushed_centrifuged")
        wash("crushed")
        wash("dust_impure")
        wash("dust_pure")

        marker {
            recipe("centrifuge_dust_pure") {
                baseType("centrifuge")
                input(material("dust_pure"), port = 0)
            }
            recipe("thermal_centrifuge_crushed_purified") {
                baseType("thermal_centrifuge")
                input(material("crushed_purified"), port = 0)
            }
            for (variant in OreVariant.entries) {
                recipe("analyze_${variant.name.lowercase()}") {
                    baseType("ore_analyzer")
                    input(variant.baseItem, port = 0)
                    voltage(variant.voltage)
                }
            }
        }

        trackJEICategory()
    }

    private fun crush(sub: String) {
        marker {
            recipe("crush_$sub") {
                baseType("macerator")
                input(material(sub), port = 0)
            }
        }
    }

    private fun wash(sub: String) {
        marker {
            recipe("wash_$sub") {
                baseType("ore_washer")
                input(material(sub), port = 0)
                input("water", port = 1)
            }
        }
    }

    private fun trackJEICategory() {
        val allTypes = buildList {
            add(modLoc("tool_crafting"))
            for (set in PROCESSING_SETS) {
                add(set.recipeType.loc())
            }
            for (set in MULTIBLOCK_SETS.values) {
                add(set.recipeType.loc())
            }
        }

        dataGen {
            for (type in allTypes) {
                trackLang(RecipeCategory.categoryTitleId(type))
            }
        }
    }
}
