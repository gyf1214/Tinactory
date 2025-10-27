package org.shsts.tinactory.datagen.content.material

import net.minecraft.world.item.Items
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder.Companion.material
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.model.IconSet.BRIGHT
import org.shsts.tinactory.datagen.content.model.IconSet.CERTUS
import org.shsts.tinactory.datagen.content.model.IconSet.DULL
import org.shsts.tinactory.datagen.content.model.IconSet.FINE
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_HORIZONTAL
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_VERTICAL
import org.shsts.tinactory.datagen.content.model.IconSet.LAPIS
import org.shsts.tinactory.datagen.content.model.IconSet.LIGNITE
import org.shsts.tinactory.datagen.content.model.IconSet.METALLIC
import org.shsts.tinactory.datagen.content.model.IconSet.QUARTZ
import org.shsts.tinactory.datagen.content.model.IconSet.ROUGH
import org.shsts.tinactory.datagen.content.model.IconSet.RUBY
import org.shsts.tinactory.datagen.content.model.IconSet.SHINY

object Materials {
    fun init() {
        woods()
        elements()
        firstDegrees()
        higherDegrees()
        ores()
        misc()
    }

    private fun woods() {
        // disable iron and wood tools
        vanilla {
            nullRecipe(
                Items.WOODEN_AXE,
                Items.WOODEN_HOE,
                Items.WOODEN_PICKAXE,
                Items.WOODEN_SHOVEL,
                Items.WOODEN_SWORD,
                Items.IRON_AXE,
                Items.IRON_HOE,
                Items.IRON_PICKAXE,
                Items.IRON_SHOVEL,
                Items.IRON_SWORD,
                Items.COMPOSTER)
        }
    }

    private fun elements() {
        material("iron", METALLIC) {
            toolProcess()
            smelt()
        }
        material("gold", SHINY) {
            toolProcess()
            smelt()
            oreProcess {
                byProducts("silver", "nickel", "silver")
            }
        }
        material("copper", SHINY) {
            toolProcess(0.75)
            smelt()
        }
        material("tin", DULL) {
            toolProcess(0.75)
            smelt()
            oreProcess()
        }
        material("sulfur", DULL)
        material("cadmium", SHINY)
        material("cobalt", METALLIC) {
            toolProcess(1.25)
            smelt()
        }
        material("nickel", METALLIC) {
            toolProcess(1.25)
            smelt()
        }
        material("magnesium", METALLIC) {
            machineProcess(Voltage.LV)
        }
        material("thorium", SHINY) {
            oreProcess {
                byProducts("lead", "rare_earth", "thorium")
            }
        }
        material("chrome", SHINY) {
            machineProcess(Voltage.MV, 1.5)
            blast(Voltage.MV, 2200, 1024) {
                component("nitrogen")
            }
        }
        material("antimony", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("silver", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
            oreProcess {
                byProducts("antimony", "antimony", "gallium")
            }
        }
        material("vanadium", METALLIC)
        material("aluminium", DULL) {
            machineProcess(Voltage.LV)
            blast(Voltage.LV, 1500, 400)
        }
        material("lead", DULL) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("zinc", METALLIC) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("gallium", SHINY) {
            machineProcess(Voltage.LV)
            smelt()
        }
        material("carbon", DULL) {
            machineProcess(Voltage.HV)
            implosion(4)
        }
        material("manganese", DULL)
        material("arsenic", DULL)
        material("silicon", METALLIC) {
            machineProcess(Voltage.HV)
            implosion(4)
        }
        material("beryllium", METALLIC) {
            machineProcess(Voltage.LV, 0.6)
            smelt()
        }
        material("sodium", METALLIC)
        material("potassium", METALLIC)
        material("calcium", METALLIC)
        material("lithium", DULL)
        material("titanium", METALLIC) {
            machineProcess(Voltage.HV, 1.25)
            blast(Voltage.HV, 2000, 960) {
                component("nitrogen")
            }
        }
        material("neodymium", METALLIC) {
            machineProcess(Voltage.HV)
            blast(Voltage.MV, 1300, 1200)
        }
        material("molybdenum", SHINY) {
            machineProcess(Voltage.HV, 1.8)
            blast(Voltage.HV, 2800, 1600) {
                component("nitrogen")
            }
        }
        material("tungsten", DULL) {
            machineProcess(Voltage.HV, 2.5)
            blast(Voltage.HV, 3600, 960) {
                component("nitrogen")
            }
        }
        material("platinum", SHINY) {
            machineProcess(Voltage.HV)
            smelt()
        }
        material("niobium", METALLIC)
    }

    private fun firstDegrees() {
        material("wrought_iron", METALLIC) {
            toolProcess()
            smelt()
        }
        material("bronze", METALLIC) {
            toolProcess(0.75)
            smelt()
            alloy(Voltage.ULV) {
                component("copper", 3)
                component("tin")
            }
        }
        material("cobaltite", METALLIC) {
            smelt("cobalt")
            centrifuge(Voltage.LV) {
                component("cobalt")
                component("arsenic")
                component("sulfur")
            }
        }
        material("invar", METALLIC) {
            toolProcess(1.25)
            smelt()
            alloy(Voltage.ULV) {
                component("iron", 2)
                component("nickel")
            }
        }
        material("cupronickel", METALLIC) {
            toolProcess(1.25)
            smelt()
            alloy(Voltage.ULV) {
                component("copper")
                component("nickel")
            }
        }
        material("steel", METALLIC) {
            toolProcess(1.5)
            blast(Voltage.ULV, 1000, 320)
            blast(Voltage.ULV, 1000, 1000, "iron")
            blast(Voltage.MV, 1000, 96, "wrought_iron") {
                component("oxygen")
            }
        }
        material("red_alloy", DULL) {
            toolProcess(0.5)
            smelt()
            alloyOnly(Voltage.ULV) {
                amount(1)
                component("copper")
                component("redstone", 4)
            }
        }
        material("battery_alloy", DULL) {
            machineProcess(Voltage.LV)
            smelt()
            alloy(Voltage.LV) {
                component("lead", 4)
                component("zinc")
            }
        }
        material("soldering_alloy", DULL) {
            fluidAlloy(Voltage.LV) {
                component("tin", 6)
                component("lead", 3)
                component("antimony")
            }
        }
        material("rutile", SHINY)
        material("brass", METALLIC) {
            toolProcess(0.75)
            smelt()
            alloy(Voltage.LV) {
                component("copper", 3)
                component("zinc")
            }
        }
        material("gallium_arsenide", DULL) {
            mix(Voltage.LV) {
                component("gallium")
                component("arsenic")
            }
        }
        material("kanthal", METALLIC) {
            mix(Voltage.LV) {
                component("iron")
                component("aluminium")
                component("chrome")
            }
            machineProcess(Voltage.LV)
            blast(Voltage.LV, 1800, 1000)
        }
        material("electrum", SHINY) {
            machineProcess(Voltage.LV, 0.75)
            smelt()
            alloy(Voltage.LV) {
                component("gold")
                component("silver")
            }
        }
        material("stainless_steel", SHINY) {
            machineProcess(Voltage.MV)
            blast(Voltage.MV, 1700, 1100)
            mix(Voltage.MV) {
                component("iron", 6)
                component("nickel")
                component("manganese")
                component("chrome")
            }
        }

        material("sodium_chloride", FINE)
        material("potassium_chloride", FINE)
        material("magnesium_chloride", DULL)
        material("calcium_chloride", FINE)
        material("lithium_chloride", FINE)
        material("ammonium_chloride", DULL)
        material("sodium_carbonate", DULL)
        material("potassium_carbonate", DULL)
        material("calcium_carbonate", DULL)
        material("lithium_carbonate", DULL)
        material("sodium_hydroxide", DULL)
        material("potassium_hydroxide", DULL)
        material("calcium_hydroxide", DULL)
        material("sodium_sulfate", DULL)
        material("potassium_nitrate", FINE)
        material("potassium_bifluoride", DULL)
        material("sulfuric_acid", DULL) {
            fluidMix(Voltage.MV, sub = "dilute") {
                component("sulfuric_acid")
                component("water")
            }
        }
        material("nickel_zinc_ferrite", METALLIC) {
            machineProcess(Voltage.MV, 1.25)
            blast(Voltage.MV, 1500, 400) {
                component("oxygen", 2)
            }
            mix(Voltage.MV) {
                component("iron", 4)
                component("nickel")
                component("zinc")
            }
        }

        material("coke", LIGNITE) {
            toolProcess()
        }
        material("charcoal", FINE) {
            toolProcess()
        }
        material("silicon_dioxide", QUARTZ) {
            smelt("glass", "primary")
        }
        material("nichrome", METALLIC) {
            machineProcess(Voltage.MV, 1.25)
            blast(Voltage.HV, 2700, 880) {
                component("nitrogen")
            }
            mix(Voltage.MV) {
                component("nickel", 4)
                component("chrome")
            }
        }
        material("aluminium_oxide", FINE)
        material("annealed_copper", BRIGHT) {
            machineProcess(Voltage.MV, 0.8)
        }
        material("tungsten_trioxide", SHINY)
        material("tungsten_carbide", DULL) {
            machineProcess(Voltage.HV, 4.0)
            blast(Voltage.HV, 3200, 1800) {
                component("nitrogen")
            }
            mix(Voltage.HV) {
                component("tungsten")
                component("carbon")
            }
        }
        material("molybdenum_trioxide", DULL)
        material("enriched_uranium_fuel", SHINY)
        material("depleted_uranium_fuel", METALLIC)
        material("niobium_titanium", DULL) {
            machineProcess(Voltage.HV, 1.2)
            blast(Voltage.HV, 3700, 1280) {
                component("nitrogen")
            }
            mix(Voltage.HV) {
                component("niobium")
                component("titanium")
            }
        }
    }

    private fun higherDegrees() {
        material("cobalt_brass", METALLIC) {
            machineProcess(Voltage.LV, 2.0)
            smelt()
            mix(Voltage.LV) {
                component("brass", 7)
                component("aluminium")
                component("cobalt")
            }
        }
        material("salt_water", DULL) {
            fluidMix(Voltage.MV) {
                component("sodium_chloride")
                component("water")
            }
        }
        material("pe", DULL) {
            machineProcess(Voltage.LV, 0.5)
        }
        material("pvc", DULL) {
            machineProcess(Voltage.LV, 0.5)
        }
        material("lpg", DULL) {
            fluidMix(Voltage.MV) {
                component("ethane")
                component("propane")
            }
        }
        material("diesel", DULL) {
            fluidMix(Voltage.MV) {
                component("light_fuel", 5)
                component("heavy_fuel")
            }
        }
        material("cetane_boosted_diesel", DULL) {
            fluidMix(Voltage.HV) {
                component("diesel", 4)
                component("ethanol", 1)
                component("nitric_acid", 1)
            }
        }
        material("vanadium_steel", METALLIC) {
            machineProcess(Voltage.MV, 1.5)
            blast(Voltage.MV, 2500, 1280) {
                component("nitrogen")
            }
        }
        material("ptfe", DULL) {
            machineProcess(Voltage.LV, 0.75)
        }
        material("ps", DULL) {
            machineProcess(Voltage.LV, 0.75)
        }
        material("fluix", CERTUS) {
            machineProcess(Voltage.MV, 1.25)
            mix(Voltage.HV) {
                component("certus_quartz")
                component("nether_quartz")
                component("redstone")
            }
            crystallize("salt_water", Voltage.HV, 600, -0.1, 1.0, 10.0)
            seeding(Voltage.MV, 1.25)
        }
        material("tungsten_steel", METALLIC) {
            machineProcess(Voltage.HV, 2.0)
            blast(Voltage.HV, 3000, 1280) {
                component("nitrogen")
            }
            mix(Voltage.HV) {
                component("steel")
                component("tungsten")
            }
        }
        material("pdms", DULL)
        material("silicone_rubber", DULL) {
            machineProcess(Voltage.MV)
        }
        material("epoxy", DULL) {
            machineProcess(Voltage.MV)
        }
        material("battery_powder", SHINY) {
            mix(Voltage.HV) {
                component("cadmium", 4)
                component("sulfuric_acid", 8, sub = "dilute")
                component("lithium", 9)
            }
        }
        material("rocket_fuel", DULL) {
            fluidMix(Voltage.HV) {
                component("lpg")
                component("oxygen", sub = "liquid")
            }
        }
    }

    private fun ores() {
        material("chalcopyrite", DULL) {
            oreProcess {
                primitive = true
                byProducts("sulfur", "cobaltite", "zinc")
            }
            smelt("copper")
        }
        material("pyrite", ROUGH) {
            oreProcess {
                primitive = true
                byProducts("sulfur", "lead", "cadmium")
            }
            smelt("iron")
        }
        material("limonite", METALLIC) {
            oreProcess {
                byProducts("nickel", "copper", "nickel")
            }
            smelt("iron")
        }
        material("banded_iron", DULL) {
            oreProcess {
                byProducts("nickel", "copper", "nickel")
            }
            smelt("iron")
        }
        material("garnierite", METALLIC) {
            oreProcess {
                byProducts("potassium_chloride", "nickel", "magnesium_chloride")
            }
            smelt("nickel")
        }
        material("coal", DULL) {
            toolProcess()
            oreProcess {
                amount = 2
                siftAndHammer = true
                byProducts("coal", "coal", "thorium")
            }
        }
        material("cassiterite", METALLIC) {
            oreProcess {
                amount = 2
                byProducts("tin")
            }
            smelt("tin")
        }
        material("redstone", DULL) {
            oreProcess {
                amount = 5
                byProducts("glowstone", "glowstone", "rare_earth")
            }
            centrifuge(Voltage.LV) {
                component("pyrite", 6)
                component("ruby", 3)
                component("silicon")
            }
        }
        material("cinnabar", SHINY) {
            oreProcess {
                byProducts("rare_earth", "glowstone", "rare_earth")
            }
            centrifuge(Voltage.LV) {
                component("mercury")
                component("sulfur")
            }
        }
        material("ruby", RUBY) {
            machineProcess(Voltage.LV, 2.0)
            oreProcess {
                siftAndHammer = true
                byProducts("chrome")
            }
        }
        material("magnetite", METALLIC) {
            oreProcess {
                byProducts("gold", "vanadium", "vanadium")
            }
            smelt("iron")
        }
        material("galena", DULL) {
            oreProcess {
                byProducts("sulfur", "antimony", "gallium")
            }
            smelt("lead")
        }
        material("sphalerite", DULL) {
            oreProcess {
                byProducts("sulfur", "antimony", "gallium")
            }
            smelt("zinc")
        }
        material("graphite", DULL) {
            oreProcess {
                byProducts("carbon")
            }
        }
        material("diamond", SHINY) {
            oreProcess {
                siftAndHammer = true
                byProducts("carbon")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("bauxite", DULL) {
            oreProcess {
                byProducts("aluminium", "gallium", "rutile")
            }
        }
        material("ilmenite", METALLIC) {
            oreProcess {
                byProducts("manganese", "manganese", "rutile")
            }
        }
        material("natural_gas", DULL) {
            fluidOre(192, Items.SAND)
        }
        material("light_oil", DULL) {
            fluidOre(240, Items.SAND)
        }
        material("heavy_oil", DULL) {
            fluidOre(512, Items.SAND)
        }
        material("emerald", SHINY) {
            oreProcess {
                siftAndHammer = true
                byProducts("aluminium", "beryllium", "thorium")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("sapphire", GEM_VERTICAL) {
            oreProcess {
                siftAndHammer = true
                byProducts("aluminium", "rutile", "rutile")
            }
            machineProcess(Voltage.LV, 2.0)
        }
        material("topaz", GEM_HORIZONTAL) {
            oreProcess {
                byProducts("blue_topaz", "aluminium", "blue_topaz")
            }
            machineProcess(Voltage.MV, 2.0)
        }
        material("blue_topaz", GEM_HORIZONTAL) {
            oreProcess {
                byProducts("topaz", "aluminium", "topaz")
            }
            machineProcess(Voltage.MV, 2.0)
        }
        material("nether_quartz", QUARTZ) {
            oreProcess {
                amount = 2
                siftPrimary = true
                byProducts("certus_quartz", "silicon_dioxide", "certus_quartz")
            }
            machineProcess(Voltage.MV)
        }
        material("certus_quartz", CERTUS) {
            oreProcess {
                byProducts("nether_quartz", "silicon_dioxide", "nether_quartz")
            }
            machineProcess(Voltage.MV)
            crystallize("salt_water", Voltage.HV, 400, -0.5, 0.5, 5.0)
            seeding(Voltage.MV)
        }
        material("lapis", LAPIS) {
            oreProcess {
                amount = 6
                siftPrimary = true
                byProducts("lapis", "silicon_dioxide", "lapis")
            }
            machineProcess(Voltage.MV)
        }
        material("platinum_group_sludge", FINE)
        material("lava", DULL) {
            fluidOre(400, Items.NETHERRACK)
        }
        material("tungstate", DULL) {
            oreProcess {
                byProducts("molybdate", "tin", "rare_earth")
            }
        }
        material("molybdate", METALLIC) {
            oreProcess {
                byProducts("tungstate", "tin", "rare_earth")
            }
        }
        material("pitchblende", DULL) {
            oreProcess {
                byProducts("lead", "rare_earth", "thorium")
            }
        }
    }

    private fun misc() {
        material("test", DULL)
        material("stone", ROUGH) {
            toolProcess()
        }
        material("flint", DULL) {
            toolProcess()
        }
        material("glass", SHINY)
        material("raw_rubber", DULL)
        material("rubber", SHINY) {
            toolProcess()
        }
        material("glowstone", SHINY)
        material("rare_earth", ROUGH)
        material("netherrack", ROUGH) {
            machineProcess(Voltage.LV, 0.5)
        }
        material("obsidian", DULL) {
            machineProcess(Voltage.HV, 4.0)
        }
        material("end_stone", DULL) {
            machineProcess(Voltage.MV)
        }
        material("blaze", FINE) {
            machineProcess(Voltage.HV)
            crystallize("lava", Voltage.HV, 1000, 0.0, 0.5)
        }
        material("ender_pearl", FINE) {
            machineProcess(Voltage.HV)
            crystallize("argon", Voltage.EV, 400, 0.5, 1.0, amount = 0.1)
        }
        material("ender_eye", SHINY) {
            machineProcess(Voltage.EV)
            crystallize("argon", Voltage.EV, 1200, 0.5, 1.5)
            centrifuge(Voltage.EV) {
                amount(1)
                component("end_stone")
                component("radon")
            }
        }
    }
}
