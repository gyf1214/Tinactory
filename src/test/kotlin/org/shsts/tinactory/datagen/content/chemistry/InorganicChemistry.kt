package org.shsts.tinactory.datagen.content.chemistry

import net.minecraft.world.item.Items
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.arcFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.distillation
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.electrolyzer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter

object InorganicChemistry {
    fun init() {
        blastOre()
        mv()
        hv()
        ev()
        ender()
        platinum()
    }

    private fun blastOre() {
        blastFurnace {
            defaults {
                voltage(Voltage.LV)
                workTicks(400)
                extra {
                    temperature(2000)
                }
            }
            input("chalcopyrite", amount = 2) {
                input("oxygen", amount = 6)
                output("iron", "ingot", 3)
                output("copper", "ingot", 3)
                output("sulfuric_acid", "gas", 6)
            }
            input("pyrite", amount = 2) {
                input("oxygen", amount = 3)
                output("iron", "ingot", 3)
                output("sulfuric_acid", "gas", 3)
            }
            input("limonite", amount = 8) {
                input("carbon", amount = 9)
                output("iron", "ingot", 12)
                output("carbon_dioxide", amount = 9)
                workTicks(1600)
            }
            input("banded_iron", amount = 8) {
                input("carbon", amount = 9)
                output("iron", "ingot", 12)
                output("carbon_dioxide", amount = 9)
                workTicks(1600)
            }
            input("garnierite", amount = 4) {
                input("carbon", amount = 3)
                output("nickel", "ingot", 6)
                output("carbon_dioxide", amount = 3)
                workTicks(800)
            }
            input("cassiterite", amount = 2) {
                input("carbon", amount = 3)
                output("tin", "ingot", 3)
                output("carbon_dioxide", amount = 3)
            }
            input("galena", amount = 2) {
                input("oxygen", amount = 3)
                output("lead", "ingot", 3)
                output("antimony", "ingot", 1)
                output("sulfuric_acid", "gas", 3)
            }
            input("sphalerite", amount = 2) {
                input("oxygen", amount = 3)
                output("zinc", "ingot", 3)
                output("silver", "ingot", 1)
                output("sulfuric_acid", "gas", 3)
            }
        }
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
            output("hydrogen_chloride", amount = 2, suffix = "from_sulfuric") {
                input("sulfuric_acid")
                input("sodium_chloride", amount = 2)
                output("sodium_sulfate")
                workTicks(320)
            }
            output("hydrogen_fluoride") {
                input("hydrogen", amount = 0.5)
                input("fluorine", amount = 0.5)
                workTicks(32)
            }
            output("carbon_dioxide") {
                input("carbon")
                input("oxygen")
                workTicks(240)
            }

            // sodium
            output("salt_water", amount = 2) {
                input("sodium_hydroxide")
                input("hydrogen_chloride")
                workTicks(32)
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
            output("sodium_hydroxide") {
                input("sodium")
                input("water")
                output("hydrogen", amount = 0.5)
                workTicks(64)
            }
            output("sodium_hydroxide", amount = 2, suffix = "_from_carbonate") {
                input("calcium_hydroxide")
                input("sodium_carbonate")
                output("calcium_carbonate")
                workTicks(128)
            }
            output("sodium_sulfate") {
                input("sodium_hydroxide", amount = 2)
                input("sulfuric_acid", "dilute", 2)
                output("water", amount = 3)
                workTicks(64)
            }
            output("sodium_sulfate", suffix = "_from_carbonate") {
                input("sodium_carbonate")
                input("sulfuric_acid", "dilute", 2)
                output("water", amount = 2)
                output("carbon_dioxide")
                workTicks(160)
            }

            // potassium
            output("potassium_chloride") {
                input("potassium_hydroxide")
                input("hydrogen_chloride")
                output("water")
                workTicks(32)
            }
            output("potassium_chloride", amount = 2, suffix = "_from_carbonate") {
                input("potassium_carbonate")
                input("hydrogen_chloride", amount = 2)
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("potassium_carbonate") {
                input("potassium_hydroxide", amount = 2)
                input("carbon_dioxide")
                output("water")
                workTicks(128)
            }
            output("potassium_carbonate", suffix = "_from_sodium") {
                input("potassium_chloride", amount = 2)
                input("sodium_carbonate")
                output("sodium_chloride", amount = 2)
                workTicks(128)
            }
            output("potassium_hydroxide") {
                input("potassium")
                input("water")
                output("hydrogen", amount = 0.5)
                workTicks(64)
            }
            output("potassium_hydroxide", amount = 2, suffix = "_from_carbonate") {
                input("calcium_hydroxide")
                input("potassium_carbonate")
                output("calcium_carbonate")
                workTicks(128)
            }
            output("potassium_nitrate", amount = 2) {
                input("nitric_acid", amount = 2)
                input("potassium_carbonate")
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("potassium_bifluoride") {
                input("potassium_hydroxide")
                input("hydrogen_fluoride", amount = 2)
                output("water")
                workTicks(64)
            }
            output("potassium_bifluoride", amount = 2, suffix = "_from_carbonate") {
                input("potassium_carbonate")
                input("hydrogen_fluoride", amount = 4)
                output("water")
                output("carbon_dioxide")
                workTicks(320)
            }

            // calcium
            output("calcium_chloride") {
                input("calcium_hydroxide")
                input("hydrogen_chloride", amount = 2)
                output("water", amount = 2)
                workTicks(64)
            }
            output("calcium_chloride", suffix = "_from_carbonate") {
                input("calcium_carbonate")
                input("hydrogen_chloride", amount = 2)
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }
            output("calcium_carbonate") {
                input("calcium_hydroxide")
                input("carbon_dioxide")
                output("water")
                workTicks(128)
            }
            output("calcium_carbonate", suffix = "_from_sodium") {
                input("calcium_chloride")
                input("sodium_carbonate")
                output("sodium_chloride", amount = 2)
                workTicks(128)
            }
            output("calcium_hydroxide") {
                input("calcium", amount = 2)
                input("water", amount = 2)
                output("hydrogen")
                workTicks(128)
            }
            output("calcium_hydroxide", suffix = "_from_carbonate") {
                input("calcium_carbonate")
                input("water", "gas", 1)
                output("carbon_dioxide")
                workTicks(400)
            }

            // lithium
            output("lithium_chloride", amount = 2) {
                input("lithium_carbonate")
                input("hydrogen_chloride", amount = 2)
                output("water")
                output("carbon_dioxide")
                workTicks(160)
            }

            // sulfur
            output("sulfuric_acid", "gas") {
                input("sulfur")
                input("oxygen")
                workTicks(480)
            }
            output("sulfuric_acid") {
                input("sulfuric_acid", "gas")
                input("water")
                input("oxygen", amount = 0.5)
                workTicks(64)
            }
            output("sulfuric_acid", suffix = "_from_hydrogen_sulfide") {
                input("hydrogen_sulfide")
                input("oxygen", amount = 2)
                workTicks(160)
            }
            output("hydrogen_sulfide") {
                input("sulfur")
                input("hydrogen")
                workTicks(200)
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

            // nitric
            output("nitric_acid") {
                input("nitric_acid", "gas")
                input("water", amount = 0.5)
                input("oxygen", amount = 0.25)
                workTicks(160)
            }
            output("nitric_acid", suffix = "_from_ammonium") {
                input("ammonia")
                input("oxygen", amount = 2)
                output("water")
                workTicks(320)
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

            output("iron_chloride") {
                input("iron")
                input("hydrogen_chloride", amount = 3)
                output("hydrogen", amount = 1.5)
                workTicks(160)
            }

            output("obsidian", "primary") {
                input("lava")
                input("water")
                output("water", "gas")
                workTicks(128)
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
                workTicks(400)
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
                workTicks(480)
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
                output("cobaltite", rate = 0.2)
                output("manganese", rate = 0.1)
                output("vanadium", rate = 0.05)
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
                input("hydrogen_chloride", amount = 2)
                output("magnesium_chloride")
                output("silicon_dioxide")
                output("mercury", amount = 0.8)
                workTicks(160)
            }
            output("tungsten_trioxide") {
                input("tungstate")
                input("hydrogen_chloride", amount = 2)
                output("lithium_brine", amount = 4)
                workTicks(400)
            }
            output("molybdenum_trioxide") {
                input("molybdate")
                input("hydrogen_chloride", amount = 2)
                output("calcium_chloride")
                output("water", amount = 2)
                workTicks(400)
            }
            output("uranium_hexafluoride") {
                input("pitchblende")
                input("hydrogen_fluoride", amount = 4)
                input("fluorine")
                output("water", amount = 2)
                workTicks(320)
                requireMultiblock()
            }

            for (name in listOf("enriched", "depleted")) {
                output("${name}_uranium_fuel") {
                    input("uranium_hexafluoride", name)
                    input("hydrogen")
                    input("water", amount = 2)
                    output("hydrogen_fluoride", amount = 6)
                    workTicks(400)
                }
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
                input("obsidian")
                input("mercury")
                output("niobium", rate = 0.3)
                output("platinum_metallic", rate = 0.1)
                workTicks(400)
                extra {
                    temperature(1300)
                }
            }
            output("tungsten", "ingot_hot", 2, suffix = "_from_tungsten_trioxide") {
                input("tungsten_trioxide", amount = 2)
                input("carbon", amount = 3)
                input("nitrogen", amount = 2)
                output("carbon_dioxide", amount = 3)
                workTicks(2560)
                extra {
                    temperature(3600)
                }
            }
            output("molybdenum", "ingot_hot", 2, suffix = "_from_molybdenum_trioxide") {
                input("molybdenum_trioxide", amount = 2)
                input("carbon", amount = 3)
                input("nitrogen", amount = 2)
                output("carbon_dioxide", amount = 3)
                workTicks(2560)
                extra {
                    temperature(2800)
                }
            }
            output("platinum", "nugget", 2, suffix = "_from_sludge") {
                input("platinum_metallic")
                input("nitrogen")
                workTicks(640)
                extra {
                    temperature(2300)
                }
            }
        }

        centrifuge {
            defaults {
                voltage(Voltage.HV)
            }
            input("uranium_hexafluoride") {
                output("uranium_hexafluoride", amount = 0.9)
                output("uranium_hexafluoride", "enriched", amount = 0.01)
                output("uranium_hexafluoride", "depleted", amount = 0.09)
                workTicks(200)
            }
        }
    }

    private fun ev() {
        electrolyzer {
            defaults {
                voltage(Voltage.EV)
            }
            input("hydrogen_fluoride") {
                input("potassium_bifluoride")
                output("fluorine", amount = 1.5)
                output("potassium")
                output("hydrogen")
                workTicks(800)
            }
        }
    }

    // ender processing
    private fun ender() {
        blastFurnace {
            output("blaze", "seed") {
                input("glowstone")
                input("lava")
                voltage(Voltage.HV)
                workTicks(720)
                extra {
                    temperature(3100)
                }
            }
            output("end_stone", "slurry") {
                input("end_stone")
                input("mercury")
                output("platinum_metallic", rate = 0.2)
                output("ender_pearl", "seed", rate = 0.1)
                voltage(Voltage.EV)
                workTicks(256)
                extra {
                    temperature(2100)
                }
            }
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.EV)
                tech(Technologies.ENDER_CHEMISTRY)
            }
            output("ender_eye", "seed") {
                input("ender_pearl", "gem")
                input("blaze", "dust")
                workTicks(120)
            }
            output("ender_eye") {
                input("ender_pearl")
                input("radon", amount = 0.75)
                workTicks(240)
            }
        }

        mixer {
            defaults {
                workTicks(64)
            }
            output("blaze", "seed") {
                input("glowstone")
                input("blaze")
                voltage(Voltage.HV)
            }
            output("ender_pearl", "seed", 3) {
                input("ender_pearl")
                input("end_stone", amount = 2)
                voltage(Voltage.EV)
            }
        }

        macerator {
            output("blaze", "dust", 3) {
                input("blaze", "gem")
                voltage(Voltage.HV)
                workTicks(128)
            }
        }
    }

    private fun platinum() {
        chemicalReactor {
            defaults {
                voltage(Voltage.HV)
                tech(Technologies.PLATINUM_GROUP_METAL)
            }
            input("platinum_metallic", amount = 2) {
                input("aqua_regia", amount = 14)
                output("platinum_palladium_leachate")
                output("rhodium_metallic")
                output("nitric_acid", "gas", 3)
                workTicks(320)
            }
            input("platinum_palladium_leachate", amount = 2) {
                input("ammonium_chloride", amount = 6)
                output("chloroplatinate", amount = 2)
                output("palladium_rich_ammonia")
                output("raw_platinum")
                output("hydrogen_chloride", amount = 6)
                workTicks(128)
            }
            output("platinum") {
                input("chloroplatinate")
                input("calcium", amount = 2)
                output("ammonia", amount = 2)
                output("calcium_chloride", amount = 2)
                workTicks(256)
            }
            output("palladium") {
                input("palladium_rich_ammonia")
                input("sodium", amount = 4)
                input("sodium_hydroxide", amount = 4)
                output("ammonia", amount = 2)
                output("salt_water", amount = 16)
                workTicks(196)
            }
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.EV)
                tech(Technologies.PLATINUM_GROUP_METAL)
            }
            input("raw_rhodium", amount = 5) {
                input("hydrogen_chloride", amount = 12)
                output("rhodium_chloride", amount = 3)
                output("raw_ruthenium", amount = 2)
                output("sulfuric_acid", "gas", 5)
                output("salt_water", amount = 5)
                workTicks(196)
            }
            input("rhodium_chloride", amount = 2) {
                input("calcium", amount = 3)
                output("rhodium", amount = 2)
                output("calcium_chloride", amount = 3)
                workTicks(384)
            }
            input("rarest_metallic", amount = 2) {
                input("mercury", amount = 3)
                output("raw_iridium")
                output("osmium_solution", amount = 3)
                workTicks(480)
            }
        }

        blastFurnace {
            defaults {
                voltage(Voltage.EV)
            }
            input("rhodium_metallic", amount = 16) {
                input("sodium_sulfate")
                input("sulfuric_acid", amount = 3)
                output("raw_rhodium", amount = 4)
                output("rarest_metallic")
                workTicks(800)
                extra {
                    temperature(3500)
                }
            }
            input("raw_ruthenium", amount = 16) {
                input("carbon", amount = 3)
                output("ruthenium", amount = 16)
                workTicks(2000)
                extra {
                    temperature(2000)
                }
            }
        }

        sifter {
            input("raw_platinum", "dust") {
                output("platinum_metallic", "dust", rate = 0.95)
                voltage(Voltage.HV)
                workTicks(160)
            }
        }

        distillation {
            input("osmium_solution", amount = 12) {
                output("osmium")
                output("rarest_metallic", amount = 3)
                output("mercury", amount = 8)
                voltage(Voltage.IV)
                workTicks(2400)
            }
        }
    }
}
