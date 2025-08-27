package org.shsts.tinactory.datagen.content.material

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder.Companion.material
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.model.IconSet.BRIGHT
import org.shsts.tinactory.datagen.content.model.IconSet.DULL
import org.shsts.tinactory.datagen.content.model.IconSet.FINE
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_HORIZONTAL
import org.shsts.tinactory.datagen.content.model.IconSet.GEM_VERTICAL
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
                Items.COMPOSTER,
            )
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
        material("thorium", SHINY)
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
        }
        material("manganese", DULL)
        material("arsenic", DULL)
        material("silicon", METALLIC)
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
            blast(Voltage.MV, 1300, 2400)
        }
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
            blast(Voltage.ULV, 1000, 800)
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
                component("antimony")
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
        material("sodium_sulfate", DULL)
        material("potassium_nitrate", FINE)
        material("sodium_hydroxide", DULL)
        material("calcium_hydroxide", DULL)
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
        material("vanadium_steel", METALLIC) {
            machineProcess(Voltage.MV, 1.5)
            blast(Voltage.MV, 2500, 1280) {
                component("nitrogen")
            }
        }
        material("ptfe", DULL) {
            machineProcess(Voltage.LV, 0.75)
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
                byProducts("sulfur", "copper", "cadmium")
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
                byProducts("magnesium_chloride", "magnesium_chloride", "nickel")
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
                byProducts("gold", "vanadium", "copper")
            }
            smelt("iron")
        }
        material("galena", DULL) {
            oreProcess {
                byProducts("sulfur", "antimony", "silver")
            }
            smelt("lead")
        }
        material("sphalerite", DULL) {
            oreProcess {
                byProducts("sulfur", "silver", "gallium")
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
            oilOre(192)
        }
        material("light_oil", DULL) {
            oilOre(240)
        }
        material("heavy_oil", DULL) {
            oilOre(512)
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
        }
        material("blue_topaz", GEM_HORIZONTAL) {
            oreProcess {
                byProducts("topaz", "aluminium", "topaz")
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
        material("raw_rubber", DULL)
        material("rubber", SHINY) {
            toolProcess()
        }
        material("glowstone", SHINY)
        material("rare_earth", ROUGH)
        material("glass", SHINY)
    }
}
