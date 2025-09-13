package org.shsts.tinactory.datagen.content.chemistry

import net.minecraft.world.item.Items
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.arcFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.distillation
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.electrolyzer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer

object InorganicChemistry {
    fun init() {
        mv()
        hv()
    }

    private fun mv() {
        mixer {
            output(Items.GUNPOWDER, 6) {
                input("sulfur")
                input("potassium_nitrate", amount = 2)
                input("carbon", amount = 3)
                voltage(Voltage.MV)
                workTicks(384)
            }
        }

        assembler {
            output(Items.TNT) {
                input(Items.GUNPOWDER, 5)
                input(Items.SAND, 4)
                voltage(Voltage.MV)
                workTicks(100)
                tech(Technologies.CHEMISTRY)
            }
        }

        distillation {
            defaults {
                voltage(Voltage.MV)
            }
            input("air", "liquid") {
                output("nitrogen", amount = 0.78)
                output("oxygen", amount = 0.21)
                output("argon", amount = 0.01)
                workTicks(96)
            }
            input("sea_water", amount = 10) {
                output("sodium_chloride", amount = 5)
                output("potassium_chloride")
                output("magnesium_chloride", rate = 0.5)
                output("calcium_chloride", rate = 0.2)
                output("water", "gas", 6.4)
                output("lithium_brine", amount = 0.1)
                workTicks(2000)
            }
            input("salt_water", amount = 2) {
                output("sodium_chloride")
                output("water", "gas")
                workTicks(320)
            }
            input("sulfuric_acid", "dilute", 2) {
                output("sulfuric_acid")
                output("water", "gas")
                workTicks(320)
            }
            input("water") {
                output("water", "gas")
                workTicks(300)
            }
        }

        electrolyzer {
            defaults {
                voltage(Voltage.MV)
            }
            input("water") {
                output("hydrogen")
                output("oxygen", amount = 0.5)
                workTicks(800)
            }
            input("salt_water", amount = 2) {
                output("hydrogen", amount = 0.5)
                output("chlorine", amount = 0.5)
                output("sodium_hydroxide")
                workTicks(400)
            }
            input("sea_water", amount = 2) {
                output("hydrogen", amount = 0.5)
                output("chlorine", amount = 0.5)
                output("sodium_hydroxide")
                workTicks(1600)
            }
            input("bauxite", amount = 6) {
                output("aluminium", amount = 6)
                output("oxygen", amount = 4.5)
                output("rutile")
                workTicks(640)
            }
            input("charcoal") {
                output("carbon")
                workTicks(64)
            }
            input("coal") {
                output("carbon", amount = 2)
                workTicks(40)
            }
            input("coke") {
                output("carbon", amount = 2)
                workTicks(32)
            }
            input("graphite") {
                output("carbon", amount = 4)
                workTicks(64)
            }
            input("silicon_dioxide") {
                output("silicon")
                output("oxygen")
                workTicks(480)
            }
            input("aluminium_oxide") {
                output("aluminium")
                output("oxygen", amount = 0.75)
                workTicks(96)
            }
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.MV)
                tech(Technologies.CHEMISTRY)
            }
            output("hydrogen_chloride") {
                input("hydrogen", amount = 0.5)
                input("chlorine", amount = 0.5)
                workTicks(64)
            }
            output("carbon_dioxide") {
                input("carbon")
                input("oxygen")
                workTicks(240)
            }
            output("calcium_carbonate") {
                input("sodium_carbonate")
                input("calcium_chloride")
                output("sodium_chloride", amount = 2)
                workTicks(64)
            }
            output("salt_water", amount = 4, suffix = "_from_carbonate") {
                input("sodium_carbonate")
                input("hydrogen_chloride", amount = 2)
                input("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("sodium_carbonate") {
                input("sodium_hydroxide", amount = 2)
                input("carbon_dioxide")
                output("water")
                workTicks(128)
            }
            output("salt_water", amount = 2) {
                input("sodium_hydroxide")
                input("hydrogen_chloride")
                workTicks(32)
            }
            output("calcium_hydroxide") {
                input("calcium_carbonate")
                input("water", "gas", 1)
                output("carbon_dioxide")
                workTicks(400)
            }
            output("calcium_chloride", suffix = "_from_carbonate") {
                input("calcium_carbonate")
                input("hydrogen_chloride", amount = 2)
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("calcium_chloride") {
                input("calcium_hydroxide")
                input("hydrogen_chloride", amount = 2)
                output("water", amount = 2)
                workTicks(32)
            }
            output("sodium_hydroxide", amount = 2) {
                input("calcium_hydroxide")
                input("sodium_carbonate")
                output("calcium_carbonate")
                workTicks(128)
            }
            output("sulfuric_acid", "gas", 1) {
                input("sulfur")
                input("oxygen", amount = 1.5)
                workTicks(480)
            }
            output("sulfuric_acid", amount = 1) {
                input("sulfuric_acid", "gas", 1)
                input("water", amount = 1)
                workTicks(64)
            }
            output("potassium_carbonate") {
                input("potassium_chloride", amount = 2)
                input("sodium_carbonate")
                output("sodium_chloride", amount = 2)
                workTicks(128)
            }
            output("potassium_nitrate", amount = 2) {
                input("nitric_acid", amount = 2)
                input("potassium_carbonate")
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("sodium_sulfate") {
                input("sulfuric_acid")
                input("sodium_chloride", amount = 2)
                output("hydrogen_chloride", amount = 2)
                workTicks(320)
            }
            output("sodium_sulfate", suffix = "_from_carbonate") {
                input("sodium_carbonate")
                input("sulfuric_acid", "dilute", 2)
                output("water", amount = 2)
                output("carbon_dioxide")
                workTicks(160)
            }
            output("sodium_sulfate", suffix = "_from_hydroxide") {
                input("sodium_hydroxide", amount = 2)
                input("sulfuric_acid", "dilute", 2)
                output("water", amount = 3)
                workTicks(64)
            }
            output("sulfur", amount = 4) {
                input("sodium_sulfate")
                input("hydrogen_sulfide", amount = 3)
                input("hydrogen_chloride", amount = 2)
                output("salt_water", amount = 4)
                workTicks(320)
            }
            output("sulfur", amount = 2, suffix = "_from_carbon") {
                input("sodium_sulfate", amount = 2)
                input("carbon", amount = 3)
                input("hydrogen_chloride", amount = 4)
                input("water", "gas", 2)
                output("salt_water", amount = 8)
                output("carbon_dioxide", amount = 3)
                workTicks(640)
            }
            output("iron_chloride") {
                input("iron")
                input("hydrogen_chloride", amount = 3)
                output("hydrogen", amount = 1.5)
                workTicks(160)
            }
            output("ammonium_chloride") {
                input("ammonia")
                input("hydrogen_chloride")
                workTicks(64)
            }
            output("ammonia", suffix = "_from_ammonium_chloride") {
                input("ammonium_chloride")
                output("hydrogen_chloride")
                workTicks(320)
            }
            output("lithium_chloride", amount = 2) {
                input("lithium_carbonate")
                input("hydrogen_chloride", amount = 2)
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("obsidian", "block") {
                input("lava")
                input("water")
                output("water", "gas")
                workTicks(32)
            }
        }
    }

    private fun hv() {
        electrolyzer {
            defaults {
                voltage(Voltage.HV)
            }
            input("sodium_chloride") {
                output("sodium")
                output("chlorine", amount = 0.5)
                workTicks(400)
            }
            input("potassium_chloride") {
                output("potassium")
                output("chlorine", amount = 0.5)
                workTicks(480)
            }
            input("magnesium_chloride") {
                output("magnesium")
                output("chlorine")
                workTicks(320)
            }
            input("calcium_chloride") {
                output("calcium")
                output("chlorine")
                workTicks(320)
            }
            input("lithium_chloride") {
                output("lithium")
                output("chlorine", amount = 0.5)
                workTicks(400)
            }
        }

        arcFurnace {
            defaults {
                voltage(Voltage.HV)
            }
            output("wrought_iron") {
                input("iron")
                input("oxygen", amount = 0.05)
                workTicks(64)
            }
            output("annealed_copper") {
                input("copper")
                input("oxygen", amount = 0.075)
                workTicks(96)
            }
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.HV)
                tech(Technologies.HYDROMETALLURGY)
            }
            input("ruby", amount = 2) {
                input("hydrogen_chloride", amount = 2)
                input("sodium_hydroxide", amount = 2)
                output("aluminium_oxide", amount = 2)
                output("salt_water", amount = 4)
                output("chrome")
                workTicks(320)
            }
            input("sapphire") {
                input("hydrogen_chloride")
                input("sodium_hydroxide")
                output("aluminium_oxide")
                output("salt_water", amount = 2)
                workTicks(160)
            }
            input("topaz", amount = 2) {
                input("hydrogen_chloride", amount = 6)
                input("sodium_hydroxide", amount = 6)
                output("aluminium_oxide", amount = 2)
                output("silicon_dioxide")
                output("salt_water", amount = 12)
                output("hydrogen_fluoride", amount = 2)
                workTicks(480)
            }
            input("blue_topaz", amount = 2) {
                input("hydrogen_chloride", amount = 6)
                input("sodium_hydroxide", amount = 6)
                output("aluminium_oxide", amount = 2)
                output("silicon_dioxide")
                output("salt_water", amount = 12)
                output("hydrogen_fluoride", amount = 1)
                workTicks(480)
            }
            input("rare_earth") {
                input("sulfuric_acid", "dilute", 0.6)
                output("cadmium", rate = 0.6)
                output("neodymium", rate = 0.4)
                output("rare_earth", "slurry", 0.6)
                workTicks(240)
            }
            input("netherrack") {
                input("sulfuric_acid", "dilute")
                output("manganese", rate = 0.4)
                output("vanadium", rate = 0.2)
                output("molybdenum", rate = 0.2)
                output("netherrack", "slurry")
                workTicks(160)
            }
            input("netherrack", "slurry", 6) {
                input("sodium_hydroxide", amount = 6)
                output("sodium_sulfate", amount = 3)
                output("silicon_dioxide", amount = 3)
                output("banded_iron", amount = 4)
                output("water", amount = 6)
                workTicks(320)
            }
            input("obsidian", "slurry") {
                input("hydrogen_chloride", amount = 4)
                input("sodium_hydroxide", amount = 2)
                output("magnesium_chloride", amount = 2)
                output("sodium_sulfate")
                output("silicon_dioxide")
                output("water", amount = 3)
                workTicks(320)
            }
            output("tungsten_trioxide") {
                input("tungstate")
                input("hydrogen_chloride", amount = 2)
                output("lithium_brine", amount = 4)
                workTicks(400)
            }
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.HV)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
            output("ammonia") {
                input("nitrogen", amount = 0.5)
                input("hydrogen", amount = 1.5)
                input("iron", "dust_tiny", 1)
                workTicks(512)
            }
            output("sulfuric_acid", suffix = "_from_hydrogen_sulfide") {
                input("hydrogen_sulfide")
                input("oxygen", amount = 2)
                requireMultiblock()
                workTicks(160)
            }
            output("sulfuric_acid", suffix = "_from_sulfur") {
                input("sulfur")
                input("water")
                input("oxygen", amount = 1.5)
                requireMultiblock()
                workTicks(240)
            }
            output("sodium_carbonate", suffix = "_from_salt_water") {
                input("salt_water", amount = 2)
                input("ammonia")
                input("carbon_dioxide")
                output("ammonium_chloride")
                workTicks(160)
            }
            output("nitric_acid") {
                input("ammonia")
                input("oxygen", amount = 2)
                output("water")
                requireMultiblock()
                workTicks(256)
            }
            output("lithium_carbonate") {
                input("lithium_brine", amount = 4)
                input("sodium_carbonate")
                output("salt_water", amount = 4)
                workTicks(128)
            }
            output("titanium_tetrachloride") {
                input("rutile")
                input("chlorine", amount = 2)
                input("carbon")
                output("carbon_dioxide")
                workTicks(320)
            }
        }

        blastFurnace {
            defaults {
                voltage(Voltage.HV)
            }
            output("titanium", "ingot_hot", suffix = "_from_titanium_tetrachloride") {
                input("magnesium", amount = 3)
                input("titanium_tetrachloride")
                output("magnesium_chloride", amount = 2)
                workTicks(800)
                extra {
                    temperature(2300)
                }
            }
            output("obsidian", "slurry") {
                input("obsidian", amount = 2)
                input("sulfuric_acid")
                output("platinum_group_sludge", rate = 0.1)
                workTicks(400)
                extra {
                    temperature(1300)
                }
            }
            output("tungsten", "ingot_hot", suffix = "_from_tungsten_trioxide") {
                input("tungsten_trioxide", amount = 2)
                input("carbon", amount = 3)
                input("nitrogen", amount = 2)
                output("carbon_dioxide", amount = 3)
                workTicks(2560)
                extra {
                    temperature(3600)
                }
            }
        }
    }
}
