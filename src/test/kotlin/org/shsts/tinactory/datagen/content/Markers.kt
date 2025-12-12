package org.shsts.tinactory.datagen.content

import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeType
import org.shsts.tinactory.AllRecipes.PROCESSING_TYPES
import org.shsts.tinactory.AllRecipes.getTypeInfo
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.MarkerBuilder
import org.shsts.tinactory.datagen.content.builder.MarkerFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.marker
import org.shsts.tinactory.integration.jei.category.RecipeCategory

object Markers {
    fun init() {
        dataGen {
            tag(AllTags.material("ingot"), AllTags.EXTRUDER_INPUT)
            tag(AllTags.material("sheet"), AllTags.EXTRUDER_INPUT)
        }

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

            wash("crushed", "crushed_purified") {
                output(AllTags.ORE_BASE_DUST, port = 2)
                output(AllTags.material("dust"), port = 2)
            }
            wash("dust_impure", "dust")
            wash("dust_pure", "dust")
            oreProcess("macerator", "raw")
            oreProcess("macerator", "crushed")
            oreProcess("macerator", "crushed_purified")
            oreProcess("macerator", "crushed_centrifuged")
            oreProcess("centrifuge", "raw_fluid")
            oreProcess("centrifuge", "dust_pure")
            oreProcess("thermal_centrifuge", "crushed")
            oreProcess("thermal_centrifuge", "crushed_purified")

            extrude("stick", "rod")
            extrude("plate")
            extrude("ring")
            extrude("wire")
            extrude("bolt")
            extrude("gear")
            extrude("rotor")
            extrude("pipe", "pipe.normal")

            solidifier("ingot")
            solidifier("dust", "ball")
            solidifier("sheet", "plate")
            solidifier("nugget")
            solidifier("ring", fromMod = true)
            solidifier("plate")
            solidifier("stick", fromMod = true)
            solidifier("bolt", fromMod = true)
            solidifier("gear")
            solidifier("rotor")
            solidifier("pipe", "block")

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

    private fun MarkerFactory.extrude(sub: String, shape: String = sub) {
        recipe("extruder/material/$sub") {
            baseType("extruder")
            prefix("material/$sub")
            input(AllTags.EXTRUDER_INPUT, port = 0)
            extra {
                display(gregtech("items/metaitems/shape.extruder.$shape"))
            }
        }
    }

    private fun MarkerFactory.solidifier(sub: String, shape: String = sub, fromMod: Boolean = false) {
        recipe("fluid_solidifier/material/$sub") {
            baseType("fluid_solidifier")
            prefix("material/$sub")
            extra {
                if (fromMod) {
                    display(modLoc("items/metaitems/shape.mold.$shape"))
                } else {
                    display(gregtech("items/metaitems/shape.mold.$shape"))
                }
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

    private fun MarkerFactory.wash(sub: String, output: String, block: MarkerBuilder.() -> Unit = {}) {
        oreProcess("ore_washer", sub) {
            input("water", port = 1)
            output(AllTags.material(output), port = 2)
            block()
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
