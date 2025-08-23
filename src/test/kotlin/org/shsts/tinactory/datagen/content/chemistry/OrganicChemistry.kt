package org.shsts.tinactory.datagen.content.chemistry

import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor

object OrganicChemistry {
    fun init() {
        chemicalReactor {
            chemicalReactor {
                defaults {
                    voltage(Voltage.MV)
                    tech(Technologies.ORGANIC_CHEMISTRY)
                }
                output("ethanol", amount = 2) {
                    input("biomass", amount = 3)
                    input("water", amount = 2)
                    output("carbon_dioxide")
                    workTicks(400)
                }
                output("carbon_dioxide", suffix = "_from_methane") {
                    input("methane")
                    input("water", amount = 2)
                    output("hydrogen", amount = 4)
                    workTicks(128)
                }
                output("pe", amount = 1.5) {
                    input("ethylene", amount = 0.144)
                    input("oxygen")
                    workTicks(160)
                }
                output("vinyl_chloride") {
                    input("ethane")
                    input("chlorine", amount = 2)
                    output("hydrogen_chloride", amount = 3)
                    workTicks(256)
                }
                output("pvc", amount = 1.5) {
                    input("vinyl_chloride", amount = 0.144)
                    input("oxygen")
                    workTicks(200)
                }
                output("ethylene", suffix = "_from_ethanol") {
                    input("ethanol")
                    input("sulfuric_acid")
                    output("sulfuric_acid", "dilute", 2)
                    workTicks(240)
                }
                output("rubber", "molten", 9) {
                    input("raw_rubber", amount = 9)
                    input("sulfur")
                    workTicks(160)
                }
            }

            chemicalReactor {
                defaults {
                    voltage(Voltage.HV)
                    tech(Technologies.ADVANCED_CHEMISTRY)
                }
                output("chloroform") {
                    input("methane")
                    input("chlorine", amount = 3)
                    output("hydrogen_chloride", amount = 3)
                    workTicks(128)
                }
                output("tetra_fluoro_ethylene") {
                    input("chloroform", amount = 2)
                    input("hydrogen_fluoride", amount = 4)
                    output("hydrogen_chloride", amount = 6)
                    workTicks(480)
                }
                output("ptfe", amount = 1.5) {
                    input("tetra_fluoro_ethylene", amount = 0.144)
                    input("oxygen")
                    workTicks(160)
                }
                output("vinyl_chloride", suffix = "_from_lcr") {
                    input("ethylene")
                    input("hydrogen_chloride")
                    input("oxygen", amount = 0.5)
                    output("water")
                    workTicks(160)
                }
                output("tetra_fluoro_ethylene", suffix = "_from_lcr") {
                    input("methane", amount = 2)
                    input("chlorine", amount = 6)
                    input("hydrogen_fluoride", amount = 4)
                    output("hydrogen_chloride", amount = 12)
                    workTicks(320)
                }
                output("pe", amount = 30, suffix = "_from_lcr") {
                    input("ethylene", amount = 2.16)
                    input("oxygen", amount = 7.5)
                    input("titanium_tetrachloride", amount = 0.1)
                    workTicks(256)
                }
                output("pvc", amount = 30, suffix = "_from_lcr") {
                    input("vinyl_chloride", amount = 2.16)
                    input("oxygen", amount = 7.5)
                    input("titanium_tetrachloride", amount = 0.1)
                    workTicks(320)
                }
                output("ptfe", amount = 30, suffix = "_from_lcr") {
                    input("tetra_fluoro_ethylene", amount = 2.16)
                    input("oxygen", amount = 7.5)
                    input("titanium_tetrachloride", amount = 0.1)
                    workTicks(512)
                }
            }

        }
    }
}
