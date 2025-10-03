package org.shsts.tinactory.datagen.content

import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeType
import org.shsts.tinactory.content.AllRecipes.PROCESSING_TYPES
import org.shsts.tinactory.content.AllRecipes.getTypeInfo
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.MarkerBuilder
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
                    display(Items.FURNACE)
                }
            }
            baseMarker("alloy_smelter")
            baseMarker("arc_furnace")
            baseMarker("bender")
            baseMarker("wiremill")

            wash("crushed")
            wash("dust_impure")
            wash("dust_pure")
            oreProcess("macerator", "raw")
            oreProcess("macerator", "crushed")
            oreProcess("macerator", "crushed_purified")
            oreProcess("macerator", "crushed_centrifuged")
            oreProcess("centrifuge", "dust_pure")
            oreProcess("thermal_centrifuge", "crushed_purified")

            extrude("stick")
            extrude("plate")
            extrude("ring")
            extrude("wire")
            extrude("bolt")
            extrude("gear")
            extrude("rotor")
            extrude("pipe")

            for (variant in OreVariant.entries) {
                val name = variant.serializedName
                recipe("ore_analyzer/$name") {
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
        val typeInfo = getTypeInfo(id)
        recipe(id) {
            baseType(id)
            prefix()
            extra {
                display(typeInfo.icon.get())
            }
        }
    }

    private fun MarkerFactory.extrude(sub: String) {
        recipe("extruder/material/$sub") {
            baseType("extruder")
            prefix("material/$sub")
            input(AllTags.material("ingot"), port = 0)
            extra {
                display(AllTags.material(sub))
            }
        }
    }

    private fun MarkerFactory.oreProcess(type: String, sub: String, block: MarkerBuilder.() -> Unit = {}) {
        recipe("$type/material/$sub") {
            baseType(type)
            prefix("material/$sub")
            input(AllTags.material(sub), port = 0)
            block()
        }
    }

    private fun MarkerFactory.wash(sub: String) {
        oreProcess("ore_washer", sub) {
            input("water", port = 1)
        }
    }

    private fun trackJEICategory() {
        val allTypes = buildList {
            add(modLoc("tool_crafting"))
            for (type in PROCESSING_TYPES.values) {
                add(type.recipeType.loc())
            }
        }

        dataGen {
            for (type in allTypes) {
                trackLang(RecipeCategory.categoryTitleId(type))
            }
        }
    }
}
