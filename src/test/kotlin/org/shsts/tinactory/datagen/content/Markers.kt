package org.shsts.tinactory.datagen.content

import org.shsts.tinactory.content.AllBlockEntities
import org.shsts.tinactory.content.AllMaterials
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.marker
import org.shsts.tinactory.integration.jei.category.RecipeCategory
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

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
                input(AllMaterials.tag("dust_pure"), port = 0)
            }
            recipe("thermal_centrifuge_crushed_purified") {
                baseType("thermal_centrifuge")
                input(AllMaterials.tag("crushed_purified"), port = 0)
            }
            for (variant in OreVariant.entries) {
                recipe("analyze_${variant.name}") {
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
                input(AllMaterials.tag(sub), port = 0)
            }
        }
    }

    private fun wash(sub: String) {
        marker {
            recipe("wash_$sub") {
                baseType("ore_washer")
                input(AllMaterials.tag(sub), port = 0)
                input("water", port = 1)
            }
        }
    }

    private fun trackJEICategory() {
        val allTypes = buildList {
            for (set in AllBlockEntities.PROCESSING_SETS) {
                add(set.recipeType.loc())
            }
            add(modLoc("tool_crafting"))
            // Multiblocks
            add(modLoc("tool_crafting"))
            add(modLoc("blast_furnace"))
            add(modLoc("sifter"))
            add(modLoc("vacuum_freezer"))
            add(modLoc("distillation"))
            add(modLoc("autofarm"))
            add(modLoc("pyrolyse_oven"))
        }

        for (type in allTypes) {
            DATA_GEN.trackLang(RecipeCategory.categoryTitleId(type))
        }
    }
}
