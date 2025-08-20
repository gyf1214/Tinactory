package org.shsts.tinactory.datagen.content

import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.stoneGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer
import org.shsts.tinactory.datagen.content.material.Woods

object RecipesKt {
    fun init() {
        Woods.init()

        vacuumFreezer {
            defaults {
                voltage(Voltage.MV)
            }
            output("air", "liquid") {
                input("air", "gas")
                workTicks(200)
            }
            output("water", "liquid") {
                input("water", "gas")
                workTicks(32)
            }
        }

        stoneGenerator {
            for (variant in OreVariant.entries) {
                output(variant.baseItem) {
                    if (variant == OreVariant.STONE) {
                        voltage(Voltage.PRIMITIVE)
                    } else {
                        voltage(variant.voltage)
                    }
                }
            }
            output("water", "liquid") {
                voltage(Voltage.ULV)
            }
        }
        stoneGenerator {
            defaults {
                voltage(Voltage.MV)
            }
            output("air", "gas")
            output("sea_water", "liquid")
        }
    }
}
