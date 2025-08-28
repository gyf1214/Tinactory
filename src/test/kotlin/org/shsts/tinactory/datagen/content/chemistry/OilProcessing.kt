package org.shsts.tinactory.datagen.content.chemistry

import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.distillation

object OilProcessing {
    fun init() {
        sulfuric("refinery_gas", 192)
        sulfuric("naphtha", 240)
        sulfuric("light_fuel", 320)
        sulfuric("heavy_fuel", 432)

        distill()
    }

    private fun sulfuric(name: String, workTicks: Long) {
        chemicalReactor {
            output(name) {
                input(name, "sulfuric")
                input("hydrogen")
                output("hydrogen_sulfide")
                voltage(Voltage.MV)
                workTicks(workTicks)
                tech(Technologies.OIL_PROCESSING)
            }
        }
    }

    private fun distill() {
        distillation {
            defaults {
                voltage(Voltage.MV)
            }

            // refinery_gas recipes
            input("refinery_gas") {
                output("methane", amount = 0.4)
                output("ethane", amount = 0.6)
                output("propane", amount = 0.2)
                output("ethylene", amount = 0.1)
                workTicks(192)
            }
            input("refinery_gas", "lightly_steam_cracked") {
                output("methane", amount = 0.6)
                output("ethane", amount = 0.6)
                output("propane", amount = 0.075)
                output("ethylene", amount = 0.15)
                output("propene", amount = 0.025)
                workTicks(160)
            }
            input("refinery_gas", "severely_steam_cracked") {
                output("methane", amount = 0.95)
                output("ethane", amount = 0.25)
                output("propane", amount = 0.05)
                output("ethylene", amount = 0.325)
                output("propene", amount = 0.05)
                workTicks(144)
            }
            input("refinery_gas", "lightly_hydro_cracked") {
                output("methane", amount = 1.3)
                output("ethane", amount = 0.5)
                output("ethylene", amount = 0.05)
                workTicks(120)
            }
            input("refinery_gas", "severely_hydro_cracked") {
                output("methane", amount = 2.0)
                output("ethane", amount = 0.2)
                workTicks(100)
            }

            // naphtha recipes
            input("naphtha") {
                output("methane", amount = 0.1)
                output("ethane", amount = 0.4)
                output("propane", amount = 0.5)
                output("ethylene", amount = 0.2)
                output("propene", amount = 0.1)
                workTicks(240)
            }
            input("naphtha", "lightly_steam_cracked") {
                output("methane", amount = 0.2)
                output("ethane", amount = 0.45)
                output("propane", amount = 0.3)
                output("ethylene", amount = 0.4)
                output("propene", amount = 0.1)
                workTicks(208)
            }
            input("naphtha", "severely_steam_cracked") {
                output("methane", amount = 0.375)
                output("ethane", amount = 0.125)
                output("ethylene", amount = 0.9)
                output("propene", amount = 0.225)
                workTicks(192)
            }
            input("naphtha", "lightly_hydro_cracked") {
                output("methane", amount = 0.9)
                output("ethane", amount = 0.45)
                output("propane", amount = 0.3)
                output("ethylene", amount = 0.2)
                workTicks(160)
            }
            input("naphtha", "severely_hydro_cracked") {
                output("methane", amount = 2.35)
                output("ethane", amount = 0.625)
                output("propane", amount = 0.1)
                output("ethylene", amount = 0.1)
                workTicks(144)
            }

            // light_fuel recipes
            input("light_fuel", "lightly_steam_cracked") {
                output("propane", amount = 0.4)
                output("propene", amount = 0.525)
                output("naphtha", amount = 0.475)
                workTicks(288)
            }
            input("light_fuel", "severely_steam_cracked") {
                output("ethylene", amount = 0.65)
                output("propene", amount = 0.65)
                output("naphtha", amount = 0.2)
                workTicks(256)
            }
            input("light_fuel", "lightly_hydro_cracked") {
                output("ethane", amount = 1.05)
                output("propane", amount = 0.125)
                output("ethylene", amount = 0.15)
                output("naphtha", amount = 0.5)
                workTicks(224)
            }
            input("light_fuel", "severely_hydro_cracked") {
                output("methane", amount = 0.5)
                output("ethane", amount = 1.2)
                output("naphtha", amount = 0.5)
                workTicks(192)
            }

            // heavy_fuel recipes
            input("heavy_fuel", "lightly_hydro_cracked") {
                output("propane", amount = 0.25)
                output("naphtha", amount = 0.4)
                output("light_fuel", amount = 1.25)
                workTicks(304)
            }
            input("heavy_fuel", "severely_hydro_cracked") {
                output("ethane", amount = 0.55)
                output("propane", amount = 0.65)
                output("naphtha", amount = 0.8)
                output("light_fuel", amount = 0.1)
                workTicks(256)
            }

            // natural_gas recipes
            input("natural_gas") {
                output("refinery_gas", "sulfuric", 1.5)
                output("naphtha", "sulfuric", 0.5)
                workTicks(192)
            }

            // light_oil recipes
            input("light_oil") {
                output("refinery_gas", "sulfuric", 0.6)
                output("naphtha", "sulfuric", 0.8)
                output("light_fuel", "sulfuric", 0.5)
                output("heavy_fuel", "sulfuric", 0.1)
                workTicks(240)
            }

            // heavy_oil recipes
            input("heavy_oil") {
                output("refinery_gas", "sulfuric", 0.2)
                output("naphtha", "sulfuric", 0.2)
                output("light_fuel", "sulfuric", 0.4)
                output("heavy_fuel", "sulfuric", 1.2)
                workTicks(360)
            }
        }
    }
}
