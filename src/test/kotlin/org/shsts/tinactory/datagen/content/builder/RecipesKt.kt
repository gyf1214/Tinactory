package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.stoneGenerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer

object RecipesKt {
    fun init() {
        vacuumFreezer {
            defaults {
                voltage(Voltage.MV)
            }
            outputMaterial("air", "liquid") {
                inputMaterial("air", "gas")
                workTicks(200)
            }
            outputMaterial("water", "liquid") {
                inputMaterial("water", "gas")
                workTicks(32)
            }
        }

        stoneGenerator {
            for (variant in OreVariant.entries) {
                outputItem(variant.baseItem) {
                    if (variant == OreVariant.STONE) {
                        voltage(Voltage.PRIMITIVE)
                    } else {
                        voltage(variant.voltage)
                    }
                }
            }
            outputMaterial("water", "liquid") {
                voltage(Voltage.ULV)
            }
            defaults {
                voltage(Voltage.MV)
            }
            outputMaterial("air", "gas")
            outputMaterial("sea_water", "liquid")
        }
    }
}
