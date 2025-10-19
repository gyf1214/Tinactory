package org.shsts.tinactory.datagen.content.chemistry

import net.minecraft.world.item.Items
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.distillation
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.pyrolyseOven
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla

object OrganicChemistry {
    fun init() {
        lv()
        mv()
        hv()
    }

    private fun lv() {
        vanilla {
            nullRecipe(Items.COARSE_DIRT)
        }

        mixer {
            defaults {
                voltage(Voltage.LV)
                workTicks(160)
            }
            output(Items.DIRT) {
                input("stone")
                input("biomass")
            }
            output(Items.GRASS_BLOCK) {
                input(Items.DIRT)
                input("biomass")
            }
            output(Items.PODZOL) {
                input(Items.DIRT)
                input("carbon")
            }
            output(Items.COARSE_DIRT, 2) {
                input(Items.DIRT)
                input(Items.GRAVEL)
            }
        }

        pyrolyseOven {
            defaults {
                voltage(Voltage.LV)
                input("coal", amount = 16)
                output("creosote_oil", amount = 8)
            }
            output("coke", amount = 16) {
                workTicks(1280)
            }
            output("coke", amount = 16, suffix = "_with_nitrogen") {
                input("nitrogen", amount = 1)
                workTicks(320)
            }
        }
    }

    private fun mv() {
        mixer {
            defaults {
                voltage(Voltage.MV)
            }
            output(Items.BONE_MEAL) {
                input("calcium_carbonate")
                input("potassium_carbonate")
                workTicks(64)
            }
            output(getItem("misc/fertilizer"), 2) {
                input(Items.BONE_MEAL)
                input("ammonium_chloride")
                input("potassium_nitrate")
                workTicks(128)
            }
        }

        distillation {
            defaults {
                voltage(Voltage.MV)
            }
            input("creosote_oil", amount = 4) {
                output("carbon")
                output("ammonia", amount = 1.2)
                output("benzene", amount = 1.4)
                output("toluene", amount = 0.3)
                output("phenol", amount = 0.3)
                workTicks(1200)
            }
            input("biomass") {
                output(Items.BONE_MEAL)
                output("methane", amount = 0.6)
                output("ammonia", amount = 0.3)
                output("water", "gas", 0.3)
                workTicks(96)
            }
        }

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
            output("ethanol", suffix = "_from_ethylene") {
                input("ethylene")
                input("water")
                workTicks(320)
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
            output("rubber", "molten", 9, suffix = "_from_sbr") {
                input("sbr", amount = 9)
                input("sulfur")
                workTicks(160)
            }
            output("silicone_rubber", "molten", amount = 9) {
                input("pdms", amount = 9)
                input("sulfur")
                workTicks(160)
            }
        }
    }

    private fun hv() {
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

        chemicalReactor {
            defaults {
                voltage(Voltage.HV)
                tech(Technologies.ADVANCED_POLYMER)
            }
            output("styrene") {
                input("benzene")
                input("ethylene")
                input("water", "gas")
                output("hydrogen")
                output("water")
                workTicks(320)
            }
            output("epichlorohydrin") {
                input("propene")
                input("chlorine", amount = 2)
                input("sodium_hydroxide", amount = 3)
                input("water")
                output("salt_water", amount = 6)
                workTicks(240)
            }
            output("chloro_benzene") {
                input("benzene")
                input("chlorine")
                output("hydrogen_chloride")
                workTicks(480)
            }
            output("phenol") {
                input("chloro_benzene")
                input("water")
                output("hydrogen_chloride")
                workTicks(128)
            }
            output("phenol", suffix = "_from_cumene") {
                input("benzene")
                input("propene")
                input("oxygen")
                output("acetone")
                workTicks(256)
            }
            output("ps", amount = 30) {
                input("styrene", amount = 2.16)
                input("oxygen", amount = 7.5)
                input("titanium_tetrachloride", amount = 0.1)
                workTicks(320)
            }
            output("sbr", amount = 30) {
                input("benzene", amount = 2.16)
                input("propene", amount = 2.16)
                input("titanium_tetrachloride", amount = 0.1)
                workTicks(280)
            }
            output("pdms", amount = 7) {
                input("silicon")
                input("methane", amount = 2)
                input("chlorine", amount = 2)
                input("water")
                output("hydrogen_chloride", amount = 4)
                workTicks(96)
            }
            output("epoxy", amount = 1000.0 / 144.0) {
                input("epichlorohydrin")
                input("phenol", amount = 2)
                input("acetone")
                input("sodium_hydroxide")
                output("salt_water", amount = 2)
                output("water")
                workTicks(160)
            }
        }

        val toluene = getItem("misc/gelled_toluene")

        chemicalReactor {
            output(toluene, 12) {
                input("toluene", amount = 1.8)
                input("nitric_acid", amount = 5)
                input("sulfuric_acid", amount = 5)
                output("sulfuric_acid", "dilute", amount = 10)
                voltage(Voltage.HV)
                workTicks(360)
                tech(Technologies.TNT)
            }
        }

        assembler {
            output(Items.TNT, 2, suffix = "_from_toluene") {
                input(toluene)
                input("pe", amount = 2)
                voltage(Voltage.HV)
                workTicks(100)
                tech(Technologies.TNT)
            }
        }
    }
}
