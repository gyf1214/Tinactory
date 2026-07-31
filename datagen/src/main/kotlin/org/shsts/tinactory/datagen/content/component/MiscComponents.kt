package org.shsts.tinactory.datagen.content.component

import net.minecraft.tags.ItemTags
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllItems.STORAGE_CELLS
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.AllTags.TOOL_HAMMER
import org.shsts.tinactory.AllTags.TOOL_HANDLE
import org.shsts.tinactory.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.CircuitTier
import org.shsts.tinactory.content.electric.Circuits.circuitBoard
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.itemLoc
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItem
import org.shsts.tinactory.datagen.content.RegistryHelper.vanillaItemOrNull
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autoclave
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.chemicalReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.circuitAssembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.implosionCompressor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.laserEngraver
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.rocket
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.wiremill
import org.shsts.tinactory.datagen.content.builder.RecipeFactory
import org.shsts.tinactory.datagen.content.builder.SimpleAssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.SimpleProcessingBuilder
import org.shsts.tinactory.datagen.content.builder.VanillaRecipeFactory
import org.shsts.tinactory.datagen.content.component.CircuitComponents.chip
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS

object MiscComponents {
    fun init() {
        ulv()

        // buzzsaw
        lathe {
            defaults {
                workTicks(240)
            }
            misc("buzzsaw/basic") {
                input("cobalt_brass", "gear")
                voltage(Voltage.LV)
            }
            misc("buzzsaw/good") {
                input("vanadium_steel", "gear")
                voltage(Voltage.MV)
            }
            misc("buzzsaw/advanced") {
                input("tungsten_carbide", "gear")
                voltage(Voltage.HV)
            }
        }

        assembler {
            misc("item_filter") {
                input("steel", "plate")
                input("zinc", "foil", 8)
                voltage(Voltage.LV)
                workTicks(200)
                tech(Technologies.SIFTING)
            }
            misc("grinder/good") {
                input("diamond", "gem_flawless")
                input("steel", "plate", 8)
                input("diamond", "dust", 4)
                voltage(Voltage.MV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            misc("grinder/advanced") {
                input("tungsten_carbide", "gear")
                input("tungsten_steel", "plate", 8)
                input("tungsten_carbide", "dust", 4)
                voltage(Voltage.HV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.MATERIAL_CUTTING)
            }
            misc("mixed_metal_ingot") {
                input("aluminium", "plate")
                input("stainless_steel", "plate")
                input("chrome", "plate")
                input("soldering_alloy", amount = 2)
                voltage(Voltage.HV)
                workTicks(200)
                tech(Technologies.TNT)
            }
            misc("carbon_mesh") {
                misc("carbon_fiber", 4)
                input("epoxy", amount = 2)
                voltage(Voltage.HV)
                workTicks(256)
                tech(Technologies.CARBON_FIBER)
            }

            componentVoltage = Voltage.IV
            output(Items.END_CRYSTAL) {
                input("ender_pearl", "gem")
                circuit(1)
                component("cable", 2)
                input("platinum", "stick", 2)
                input("tungsten_steel", "plate", 3)
                input(Items.GLASS, 4)
                input("soldering_alloy")
                voltage(Voltage.EV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.ENDER_CHEMISTRY)
            }
            misc("raw_rhodium_plated_palladium", 4) {
                input("rhodium", "plate")
                input("palladium", "ingot", 3)
                input("soldering_alloy")
                voltage(Voltage.IV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.RHODIUM_PLATED_PALLADIUM)
            }
        }

        wiremill {
            misc("carbon_fiber") {
                input("carbon", "ingot", 4)
                workTicks(192)
                voltage(Voltage.HV)
            }
        }

        implosionCompressor {
            defaults {
                voltage(Voltage.HV)
            }
            misc("advanced_alloy") {
                misc("mixed_metal_ingot")
                input(Items.TNT, 12, port = 1)
            }
            misc("carbon_plate") {
                misc("carbon_mesh")
                input(Items.TNT, 12, port = 1)
            }
        }

        autoclave {
            misc("quantum_eye") {
                input("ender_eye", "gem")
                input("radon")
                voltage(Voltage.EV)
                workTicks(1600)
                requireCleanness(0.5, 2.0)
            }
            misc("quantum_star") {
                input("nether_star", "gem")
                input("naquadria", "molten")
                voltage(Voltage.LUV)
                workTicks(3200)
                requireCleanness(0.5, 2.0)
            }
        }

        researches()
        ae()
        powers()
        rockets()
        nuclear()
        colors()
        buildings()
    }

    fun <R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>> RecipeFactory<R, B>.misc(
        id: String, amount: Int = 1, block: B.() -> Unit) {
        output(getItem("component/$id"), amount, block = block)
    }

    fun ProcessingRecipeBuilder<*, *>.misc(id: String, amount: Int = 1) {
        input(getItem("component/$id"), amount)
    }

    private fun ulv() {
        val cable = getComponent("cable").item(Voltage.ULV)

        vanilla {
            shapeless(getMaterial("iron").tag("wire"),
                cable, fromAmount = 4, criteria = "has_wire")
        }

        toolCrafting {
            result(getComponent("machine_hull").item(Voltage.ULV)) {
                pattern("###")
                pattern("#W#")
                pattern("###")
                define('#', "iron", "plate")
                define('W', cable)
                toolTag(TOOL_WRENCH)
            }
            result(getComponent("fluid_cell").item(Voltage.ULV)) {
                pattern("###")
                pattern("#G#")
                pattern(" # ")
                define('#', "iron", "plate")
                define('G', "glass", "primary")
                toolTag(TOOL_HAMMER, TOOL_WRENCH)
            }
        }

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.SOLDERING)
            }
            component("machine_hull") {
                input("iron", "plate", 8)
                component("cable")
            }
            component("cable") {
                input("iron", "wire", 4)
            }
            component("fluid_cell") {
                input("iron", "plate", 4)
                input("glass", "primary")
            }
            output(Items.NAME_TAG) {
                input("iron", "plate")
                input(TOOL_HANDLE)
            }
        }
    }

    private fun researches() {
        research(Voltage.ULV) {
            input("iron", "plate")
            input("copper", "wire")
        }

        research(Voltage.LV) {
            input(getComponent("electric_motor").item(Voltage.LV))
            input("steel", "gear")
        }

        research(Voltage.MV) {
            input(getComponent("electric_pump").item(Voltage.MV))
            input(circuitBoard(CircuitTier.CPU).get())
        }

        research(Voltage.HV) {
            input(getComponent("conveyor_module").item(Voltage.HV))
            misc("advanced_alloy")
        }

        research(Voltage.EV) {
            input(getComponent("robot_arm").item(Voltage.EV))
            misc("carbon_plate")
        }

        research(Voltage.IV) {
            input(STORAGE_CELLS[1].component.get())
            input(Items.END_CRYSTAL)
        }
    }

    private fun research(voltage: Voltage, block: SimpleAssemblyRecipeBuilder.() -> Unit) {
        assembler {
            output(getComponent("research_equipment").item(voltage)) {
                voltage(voltage)
                workTicks(200)
                block()
            }
        }
    }

    private fun ProcessingRecipeFactory.storageComponent(i: Int,
        block: SimpleProcessingBuilder.() -> Unit) {
        output(STORAGE_CELLS[i].component.get()) {
            input("pvc", "sheet")
            input(AllTags.circuit(Voltage.fromRank(i + 2)))
            if (i > 0) {
                input(STORAGE_CELLS[i - 1].component.get(), 3)
            }
            block()
            input("soldering_alloy", amount = 2)
        }
    }

    private fun ae() {
        val annihilation = getItem("component/annihilation_core")
        val formation = getItem("component/formation_core")

        assembler {
            defaults {
                voltage(Voltage.HV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.DIGITAL_STORAGE)
            }
            output(annihilation, 2) {
                circuit(1, Voltage.MV)
                component("robot_arm", 1, Voltage.MV)
                input("nether_quartz", "primary", 4)
                input("fluix", "dust", 4)
                input("annealed_copper", "wire_fine", 8)
                input("pvc")
            }
            output(formation, 2) {
                circuit(1, Voltage.MV)
                component("conveyor_module", 1, Voltage.MV)
                input("certus_quartz", "gem", 4)
                input("fluix", "dust", 4)
                input("annealed_copper", "wire_fine", 8)
                input("pvc")
            }
            misc("silicon_print") {
                input("silicon", "ingot")
                input("annealed_copper", "foil", 4)
                input("fluix", "gem")
                input("epoxy", amount = 1)
                tech(Technologies.ADVANCED_POLYMER)
            }

            for (entry in STORAGE_CELLS) {
                val component = entry.component.get()
                output(entry.item.get()) {
                    input(component)
                    input(annihilation)
                    input(formation)
                    input("aluminium", "plate", 3)
                    input("soldering_alloy", amount = 3)
                }
                output(entry.fluid.get()) {
                    input(component)
                    input(annihilation)
                    input(formation)
                    input("stainless_steel", "plate", 3)
                    input("soldering_alloy", amount = 3)
                }
                output(entry.pattern.get()) {
                    input(component)
                    misc("silicon_print")
                    input("epoxy", "sheet")
                    input("titanium", "plate", 3)
                    input("soldering_alloy", amount = 2)
                    tech(Technologies.AUTOCRAFTING)
                }
            }
        }

        circuitAssembler {
            defaults {
                workTicks(200)
            }
            misc("logic_processor") {
                misc("silicon_print")
                chip("cpu")
                chip("ram", 4)
                input("electrum", "wire_fine", 8)
                input("soldering_alloy", amount = 2)
                voltage(Voltage.HV)
            }
            misc("calculation_processor") {
                misc("silicon_print")
                misc("logic_processor", 2)
                chip("nano_cpu")
                chip("nor", 8)
                input("platinum", "wire_fine", 8)
                input("soldering_alloy", amount = 3)
                voltage(Voltage.EV)
            }
            storageComponent(0) {
                chip("ram", 4)
                input("certus_quartz", "gem", 4)
                input("annealed_copper", "wire_fine", 16)
                voltage(Voltage.HV)
            }
            storageComponent(1) {
                chip("nor", 4)
                input("certus_quartz", "gem", 4)
                input("platinum", "wire_fine", 16)
                voltage(Voltage.HV)
            }
            storageComponent(2) {
                chip("nand", 4)
                input("fluix", "gem", 4)
                input("niobium_titanium", "wire_fine", 16)
                voltage(Voltage.EV)
            }
            storageComponent(3) {
                chip("nand", 16)
                input("fluix", "gem", 4)
                input("trinium", "wire_fine", 16)
                voltage(Voltage.EV)
            }
        }
    }

    private fun powers() {
        autoclave {
            misc("energy_crystal") {
                input("energy", "dust", 6)
                input("salt_water")
                voltage(Voltage.EV)
                workTicks(600)
                requireCleanness(0.0, 0.85)
            }
            misc("lapotron_crystal") {
                input("lapotron", "dust", 8)
                input("salt_water")
                voltage(Voltage.IV)
                workTicks(800)
                requireCleanness(0.5, 0.9)
            }
        }
        laserEngraver {
            misc("energy_chip") {
                misc("energy_crystal")
                input("ruby", "lens", 0, port = 1)
                voltage(Voltage.IV)
                workTicks(800)
                requireCleanness(0.4, 1.4)
            }
            misc("lapotron_chip") {
                misc("lapotron_crystal")
                input("blue_topaz", "lens", 0, port = 1)
                voltage(Voltage.LUV)
                workTicks(1200)
                requireCleanness(0.5, 1.5)
            }
        }
        assembler {
            defaults {
                workTicks(COMPONENT_TICKS)
                tech(Technologies.POWER_SUBSTATION)
                circuit(1)
            }

            componentVoltage = Voltage.LUV
            misc("lapotronic_energy_orb") {
                misc("lapotron_crystal", 8)
                misc("energy_chip", 12)
                component("field_generator", voltage = Voltage.IV)
                input("platinum", "wire_fine", 16)
                input("platinum", "bolt", 8)
                input("soldering_alloy", amount = 3)
                voltage(Voltage.LUV)
            }

            componentVoltage = Voltage.ZPM
            misc("lapotronic_energy_orb_cluster") {
                misc("lapotronic_energy_orb", 2)
                misc("lapotron_chip", 8)
                misc("energy_chip", 16)
                component("field_generator", voltage = Voltage.LUV)
                input("niobium_titanium", "wire_fine", 24)
                input("naquadah", "bolt", 16)
                input("soldering_alloy", amount = 5)
                voltage(Voltage.ZPM)
            }
        }
    }

    private fun rockets() {
        rocket {
            target(Technologies.ROCKET_T1) {
                input(AllTags.circuit(Voltage.EV))
                input(getComponent("electric_pump").item(Voltage.HV), 4)
                misc("advanced_alloy", 16)
                input("cetane_boosted_diesel")
                voltage(Voltage.HV)
            }
            target(Technologies.ROCKET_T2) {
                input(AllTags.circuit(Voltage.IV))
                input(STORAGE_CELLS[0].component.get(), 2)
                input(getComponent("electric_pump").item(Voltage.IV), 4)
                misc("advanced_alloy", 8)
                misc("carbon_plate", 16)
                input("soldering_alloy", amount = 4)
                input("rocket_fuel")
                voltage(Voltage.EV)
            }
            target(Technologies.INTERSTELLAR_TRAVEL) {
                input(AllTags.circuit(Voltage.MAX))
                input(getItem("component/lapotronic_energy_orb_cluster"), 4)
                input(getComponent("field_generator").item(Voltage.ZPM), 4)
                input(STORAGE_CELLS[3].component.get(), 4)
                input("neutronium", "plate", 32)
                input(getComponent("electric_pump").item(Voltage.ZPM), 16)
                input("soldering_alloy", amount = 16)
                input("naquadria_infused_rocket_fuel", amount = 8)
                voltage(Voltage.ZPM)
            }
        }
    }

    private fun nuclear() {
        assembler {
            defaults {
                voltage(Voltage.HV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.NUCLEAR_PHYSICS)
            }
            misc("empty_nuclear_rod") {
                input("titanium", "stick", 2)
                misc("advanced_alloy", 3)
                input("soldering_alloy", amount = 2)
            }
            misc("uranium_fuel_rod") {
                misc("empty_nuclear_rod")
                input("enriched_uranium_fuel", "dust")
            }
            misc("enriched_naquadah_fuel_rod") {
                misc("empty_nuclear_rod")
                input("enriched_naquadah", "bolt")
            }
            misc("moderator_rod") {
                misc("empty_nuclear_rod")
                input("carbon", "ingot", 16)
            }
            misc("control_rod") {
                misc("empty_nuclear_rod")
                input("silver", "ingot", 16)
            }
        }

        centrifuge {
            defaults {
                voltage(Voltage.HV)
                workTicks(320)
            }
            input(getItem("component/nuclear_waste_rod")) {
                output(getItem("component/empty_nuclear_rod"))
                output("nuclear_waste", "dust")
            }
            input(getItem("component/depleted_naquadah_fuel_rod")) {
                output(getItem("component/empty_nuclear_rod"))
                output("naquadah", "bolt")
                voltage(Voltage.IV)
                workTicks(400)
            }
        }
    }

    private val COLORS = DyeColor.entries.map(DyeColor::getName)

    private fun String.applyColor(color: String) = replace("#", color)

    private fun VanillaRecipeFactory.nullColor(pattern: String) {
        for (color in COLORS) {
            nullRecipe(pattern.applyColor(color))
        }
    }

    private fun <R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>> RecipeFactory<R, B>.colorRecipe(
        pattern: String, amount: Int = 1, dye: Int = 1, suffix: String = "", block: B.(String) -> Unit = {}) {
        for (color in COLORS) {
            output(vanillaItem(pattern.applyColor(color)), amount, suffix = suffix) {
                block(color)
                if (dye > 0) {
                    input(vanillaItem("${color}_dye"), dye)
                }
            }
        }
    }

    private fun mixDye(color: String, vararg inputs: Any, oldSuffix: String = "") {
        vanilla {
            nullRecipe("${color}_dye${oldSuffix}")
        }
        var totalCount = 0
        mixer {
            val output = vanillaItem("${color}_dye")
            recipe(itemLoc(output)) {
                var i = 0
                while (i < inputs.size) {
                    val item = vanillaItem("${inputs[i]}_dye")
                    val count: Int
                    if (i + 1 < inputs.size && inputs[i + 1] is Int) {
                        count = inputs[i + 1] as Int
                        i++
                    } else {
                        count = 1
                    }
                    input(item, count)
                    totalCount += count
                    i++
                }
                output(output, totalCount)
                voltage(Voltage.LV)
                workTicks(64L * totalCount)
            }
        }
    }

    private fun dye(outputId: String, input: ItemLike, amount: Int = 1, oldSuffix: String = "",
        newSuffix: String = "") {
        vanilla {
            nullRecipe("${outputId}_dye$oldSuffix")
        }
        chemicalReactor {
            output(vanillaItem("${outputId}_dye"), amount, suffix = newSuffix) {
                input(input)
                input("sulfuric_acid", "dilute", amount * 0.1)
                voltage(Voltage.MV)
                workTicks(64L * amount)
                tech(Technologies.CHEMISTRY)
            }
        }
    }

    private fun colors() {
        mixDye("cyan", "blue", "green")
        mixDye("gray", "black", "white")
        mixDye("light_blue", "blue", "white", oldSuffix = "_from_blue_white_dye")
        mixDye("light_gray", "gray", "white", oldSuffix = "_from_gray_white_dye")
        mixDye("lime", "green", "white")
        mixDye("magenta", "purple", "pink", oldSuffix = "_from_purple_and_pink")
        mixDye("orange", "red", "yellow", oldSuffix = "_from_red_yellow")
        mixDye("pink", "red", "white", oldSuffix = "_from_red_white_dye")
        mixDye("purple", "blue", "red")

        dye("black", Items.INK_SAC)
        dye("black", Items.WITHER_ROSE, oldSuffix = "_from_wither_rose", newSuffix = "_from_wither_rose")
        dye("blue", Items.LAPIS_LAZULI)
        dye("blue", Items.CORNFLOWER, oldSuffix = "_from_cornflower", newSuffix = "_from_cornflower")
        dye("brown", Items.COCOA_BEANS)
        dye("cyan", Items.PITCHER_PLANT, 2, "_from_pitcher_plant")
        dye("green", Items.CACTUS)
        dye("light_blue", Items.BLUE_ORCHID, oldSuffix = "_from_blue_orchid")
        dye("light_gray", Items.AZURE_BLUET, oldSuffix = "_from_azure_bluet")
        dye("light_gray", Items.OXEYE_DAISY, oldSuffix = "_from_oxeye_daisy", newSuffix = "_from_oxeye_daisy")
        dye("light_gray", Items.WHITE_TULIP, oldSuffix = "_from_white_tulip", newSuffix = "_from_white_tulip")
        dye("lime", Items.SEA_PICKLE, oldSuffix = "_from_smelting")
        dye("magenta", Items.ALLIUM, oldSuffix = "_from_allium")
        dye("magenta", Items.LILAC, 2, "_from_lilac", "_from_lilac")
        dye("orange", Items.ORANGE_TULIP, oldSuffix = "_from_orange_tulip")
        dye("orange", Items.TORCHFLOWER, oldSuffix = "_from_torchflower", newSuffix = "_from_torchflower")
        dye("pink", Items.PEONY, 2, "_from_peony")
        dye("pink", Items.PINK_PETALS, oldSuffix = "_from_pink_petals", newSuffix = "_from_pink_petals")
        dye("pink", Items.PINK_TULIP, oldSuffix = "_from_pink_tulip", newSuffix = "_from_pink_tulip")
        dye("red", Items.BEETROOT, oldSuffix = "_from_beetroot")
        dye("red", Items.POPPY, oldSuffix = "_from_poppy", newSuffix = "_from_poppy")
        dye("red", Items.ROSE_BUSH, 2, "_from_rose_bush", "_from_rose_bush")
        dye("red", Items.RED_TULIP, oldSuffix = "_from_tulip", newSuffix = "_from_tulip")
        dye("white", Items.BONE_MEAL)
        dye("white", Items.LILY_OF_THE_VALLEY, oldSuffix = "_from_lily_of_the_valley",
            newSuffix = "_from_lily_of_the_valley")
        dye("yellow", Items.DANDELION, oldSuffix = "_from_dandelion")
        dye("yellow", Items.SUNFLOWER, 2, "_from_sunflower", "_from_sunflower")

        vanilla {
            nullRecipe("light_gray_dye_from_black_white_dye")
            nullRecipe("magenta_dye_from_blue_red_pink", "magenta_dye_from_blue_red_white_dye")
            nullRecipe("glass_pane", "white_wool_from_string")

            nullColor("#_banner")
            nullColor("#_bed")
            nullColor("dye_#_bed")
            nullColor("#_candle")
            nullColor("#_carpet")
            nullColor("dye_#_carpet")
            nullColor("#_concrete_powder")
            nullColor("#_stained_glass")
            nullColor("#_stained_glass_pane")
            nullColor("#_stained_glass_pane_from_glass_pane")
            nullColor("#_terracotta")
            nullColor("dye_#_wool")
        }

        chemicalReactor {
            defaults {
                voltage(Voltage.MV)
                workTicks(64)
                tech(Technologies.CHEMISTRY)
            }
            colorRecipe("#_bed") {
                input(ItemTags.BEDS)
            }
            colorRecipe("#_candle") {
                input(Items.CANDLE)
            }
            colorRecipe("#_carpet") {
                input(ItemTags.WOOL_CARPETS)
            }
            colorRecipe("#_concrete", dye = 0) { color ->
                input(vanillaItem("${color}_concrete_powder"))
                input("water", amount = 1 / 8.0)
            }
            colorRecipe("#_stained_glass", 8) {
                input(Items.GLASS, 8)
            }
            colorRecipe("#_stained_glass_pane", 8) {
                input(Items.GLASS_PANE, 8)
            }
            colorRecipe("#_terracotta", 8) {
                input(Items.TERRACOTTA, 8)
            }
            colorRecipe("#_wool") {
                input(ItemTags.WOOL)
            }
        }

        mixer {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
            }
            colorRecipe("#_concrete_powder", 8) {
                input(Items.SAND, 4)
                input(Items.GRAVEL, 4)
            }
        }

        cutter {
            defaults {
                voltage(Voltage.LV)
            }
            output(Items.GLASS_PANE, 4) {
                input(Items.GLASS)
                input("water", amount = 0.2)
                workTicks(128)
            }
            colorRecipe("#_stained_glass_pane", 4, dye = 0) { color ->
                input(vanillaItem("${color}_stained_glass"))
                input("water", amount = 0.2)
                workTicks(128)
            }
            colorRecipe("#_carpet", 2, dye = 0) { color ->
                input(vanillaItem("${color}_wool"))
                input("water", amount = 0.05)
                workTicks(64)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
                tech(Technologies.SOLDERING)
            }
            output(Items.WHITE_WOOL) {
                input(Items.STRING, 4)
                voltage(Voltage.ULV)
            }
            colorRecipe("#_banner", dye = 0) { color ->
                input(vanillaItem("${color}_wool"), 4)
                input(TOOL_HANDLE)
            }
            colorRecipe("#_bed", dye = 0) { color ->
                input(vanillaItem("${color}_wool"), 2)
                input(ItemTags.PLANKS, 2)
            }
        }
    }

    private fun trio(baseId: String, stemId: String = baseId, cuttingInputs: List<String> = listOf(baseId)) {
        val base = vanillaItem(baseId)
        val slabId = "${stemId}_slab"
        val stairsId = "${stemId}_stairs"
        val wallId = "${stemId}_wall"
        val slab = vanillaItemOrNull(slabId)
        val stairs = vanillaItemOrNull(stairsId)
        val wall = vanillaItemOrNull(wallId)
        val trioItems = listOfNotNull(slab, stairs, wall)
        val trioOutputs = listOfNotNull(slab?.let { slabId }, stairs?.let { stairsId }, wall?.let { wallId })

        vanilla {
            nullRecipe(*trioItems.toTypedArray())
            for (output in trioOutputs) {
                for (input in cuttingInputs) {
                    nullRecipe("${output}_from_${input}_stonecutting")
                }
            }
        }
        cutter {
            if (slab != null) {
                output(slab, 2) {
                    input(base)
                    input("water", amount = 0.1)
                    voltage(Voltage.LV)
                    workTicks(80)
                }
            }
        }
        lathe {
            if (stairs != null && slab != null) {
                output(stairs) {
                    input(slab, 2)
                    voltage(Voltage.LV)
                    workTicks(80)
                }
            }
            if (wall != null) {
                output(wall) {
                    input(base)
                    voltage(Voltage.LV)
                    workTicks(80)
                }
            }
        }
    }

    private fun polish(inputId: String, outputId: String,
        cuttingInputs: List<String> = listOf(inputId), lens: String = "ruby") {
        val input = vanillaItem(inputId)
        val output = vanillaItem(outputId)

        vanilla {
            nullRecipe(output)
            for (cuttingInput in cuttingInputs) {
                nullRecipe("${outputId}_from_${cuttingInput}_stonecutting")
            }
        }

        laserEngraver {
            output(output) {
                input(input)
                input(lens, "lens", 0, port = 1)
                voltage(Voltage.LV)
                workTicks(80)
                requireCleanness(-1.0, 0.0)
            }
        }
    }

    private fun chisel(inputId: String, outputId: String,
        cuttingInputs: List<String> = listOf(inputId)) = polish(inputId, outputId, cuttingInputs, "sapphire")

    private val COPPERS = listOf("", "exposed_", "weathered_", "oxidized_")
    private val COPPER_FORMS = listOf("copper", "cut_copper", "cut_copper_slab", "cut_copper_stairs",
        "copper_grate", "copper_bulb", "copper_door", "copper_trapdoor", "chiseled_copper")

    private fun coppers() {
        assembler {
            defaults {
                voltage(Voltage.LV)
                workTicks(80)
            }
            output(Items.COPPER_BULB) {
                circuit(1, Voltage.ULV)
                input("redstone", "dust", 3)
                input("copper", "plate", 3)
            }
            output(Items.COPPER_DOOR) {
                input("copper", "ingot", 2)
                input("redstone", "dust")
            }
            output(Items.COPPER_TRAPDOOR) {
                input("copper", "plate", 2)
                input("copper", "stick", 2)
                input("redstone", "dust")
            }
            output(Items.IRON_DOOR) {
                input("iron", "ingot", 2)
                input("redstone", "dust")
            }
            output(Items.IRON_TRAPDOOR) {
                input("iron", "plate", 2)
                input("iron", "stick", 2)
                input("redstone", "dust")
            }
        }
        vanilla {
            nullRecipe(Items.IRON_DOOR, Items.IRON_TRAPDOOR)
            nullRecipe(Items.COPPER_DOOR, Items.COPPER_TRAPDOOR)
        }

        for ((index, prefix) in COPPERS.withIndex()) {
            val base = if (prefix.isEmpty()) "copper_block" else "${prefix}copper"
            val cut = "${prefix}cut_copper"
            val grate = "${prefix}copper_grate"
            val bulb = "${prefix}copper_bulb"
            val chiseled = "${prefix}chiseled_copper"

            trio(cut, cuttingInputs = listOf(cut, base))
            trio("waxed_$cut", cuttingInputs = listOf("waxed_$cut", "waxed_$base"))
            polish(base, cut)
            polish("waxed_$base", "waxed_$cut")
            chisel(base, chiseled, listOf(base, cut))
            chisel("waxed_$base", "waxed_$chiseled", listOf("waxed_$base", "waxed_$cut"))

            vanilla {
                nullRecipe("${prefix}copper_grate", "waxed_${prefix}copper_grate")
                nullRecipe("${prefix}copper_grate_from_${base}_stonecutting")
                nullRecipe("waxed_${prefix}copper_grate_from_waxed_${base}_stonecutting")
                nullRecipe(bulb, "waxed_$bulb")
            }

            cutter {
                defaults {
                    input("water", amount = 0.1)
                    voltage(Voltage.LV)
                    workTicks(80)
                }
                output(vanillaItem(grate), 4) {
                    input(vanillaItem(base))
                }
                output(vanillaItem("waxed_$grate"), 4) {
                    input(vanillaItem("waxed_$base"))
                }
            }

            if (index < COPPERS.lastIndex) {
                val nextPrefix = COPPERS[index + 1]
                chemicalReactor {
                    defaults {
                        input("oxygen", amount = 0.125)
                        voltage(Voltage.MV)
                        workTicks(160)
                        tech(Technologies.CHEMISTRY)
                    }
                    for (form in COPPER_FORMS) {
                        output(vanillaItem("$nextPrefix$form")) {
                            input(vanillaItem(if (form == "copper") base else "$prefix$form"))
                        }
                    }
                }
            }

            chemicalReactor {
                defaults {
                    input(Items.HONEYCOMB)
                    voltage(Voltage.MV)
                    workTicks(80)
                    tech(Technologies.CHEMISTRY)
                }
                for (form in COPPER_FORMS) {
                    val inputId = if (form == "copper") base else "$prefix$form"
                    output(vanillaItem("waxed_$inputId")) {
                        input(vanillaItem(inputId))
                    }
                    vanilla {
                        nullRecipe("waxed_${inputId}_from_honeycomb")
                    }
                }
            }
        }
    }

    private fun buildings() {
        vanilla {
            nullRecipe(Items.AMETHYST_BLOCK, Items.BRICKS, Items.DARK_PRISMARINE, Items.NETHER_BRICKS,
                Items.PACKED_MUD, Items.PRISMARINE, Items.RED_NETHER_BRICKS, Items.SEA_LANTERN)
        }
        assembler {
            defaults {
                voltage(Voltage.LV)
                workTicks(64)
                tech(Technologies.SOLDERING)
            }
            output(Items.AMETHYST_BLOCK) {
                input(Items.AMETHYST_SHARD, 4)
            }
            output(Items.BRICKS) {
                input(Items.BRICK, 4)
            }
            output(Items.NETHER_BRICKS) {
                input(Items.NETHER_BRICK, 4)
            }
            output(Items.PRISMARINE) {
                input(Items.PRISMARINE_SHARD, 4)
            }
            output(Items.SEA_LANTERN) {
                circuit(1, Voltage.ULV)
                input(Items.PRISMARINE_CRYSTALS, 4)
                input(Items.PRISMARINE_SHARD, 4)
            }
        }
        chemicalReactor {
            defaults {
                voltage(Voltage.MV)
                workTicks(80)
                tech(Technologies.CHEMISTRY)
            }
            output(Items.DARK_PRISMARINE) {
                input(Items.PRISMARINE)
                input(Items.BLACK_DYE)
            }
            output(Items.RED_NETHER_BRICKS) {
                input(Items.NETHER_BRICKS)
                input(Items.NETHER_WART)
            }
        }
        mixer {
            output(Items.PACKED_MUD) {
                input(Items.MUD)
                input(Items.WHEAT)
                voltage(Voltage.LV)
                workTicks(64)
            }
        }

        trio("andesite")
        trio("polished_andesite", cuttingInputs = listOf("andesite", "polished_andesite"))
        trio("blackstone")
        trio("polished_blackstone", cuttingInputs = listOf("blackstone", "polished_blackstone"))
        trio("polished_blackstone_bricks", "polished_blackstone_brick",
            cuttingInputs = listOf("blackstone", "polished_blackstone", "polished_blackstone_bricks"))
        trio("bricks", "brick")
        trio("cobbled_deepslate")
        trio("polished_deepslate", cuttingInputs = listOf("cobbled_deepslate", "polished_deepslate"))
        trio("deepslate_bricks", "deepslate_brick",
            cuttingInputs = listOf("cobbled_deepslate", "polished_deepslate", "deepslate_bricks"))
        trio("deepslate_tiles", "deepslate_tile",
            cuttingInputs = listOf("cobbled_deepslate", "polished_deepslate", "deepslate_bricks", "deepslate_tiles"))
        trio("cobblestone")
        trio("prismarine")
        // special naming 1
        trio("prismarine_bricks", "prismarine_brick", cuttingInputs = listOf("prismarine"))
        trio("dark_prismarine")
        trio("diorite")
        trio("polished_diorite", cuttingInputs = listOf("diorite", "polished_diorite"))
        trio("end_stone_bricks", "end_stone_brick", cuttingInputs = listOf("end_stone", "end_stone_brick"))
        trio("granite")
        trio("polished_granite", cuttingInputs = listOf("granite", "polished_granite"))
        trio("mossy_cobblestone")
        // special naming 2
        trio("mossy_stone_bricks", "mossy_stone_brick", cuttingInputs = listOf("mossy_stone_brick"))
        trio("mud_bricks", "mud_brick")
        trio("nether_bricks", "nether_brick")
        trio("red_nether_bricks", "red_nether_brick")
        trio("purpur_block", "purpur")
        trio("quartz_block", "quartz", cuttingInputs = emptyList())
        // special naming 3
        vanilla {
            nullRecipe("quartz_slab_from_stonecutting", "quartz_stairs_from_quartz_block_stonecutting")
        }
        trio("smooth_quartz")
        trio("sandstone")
        trio("cut_sandstone", cuttingInputs = listOf("sandstone", "cut_sandstone"))
        trio("smooth_sandstone")
        trio("red_sandstone")
        trio("cut_red_sandstone", cuttingInputs = listOf("red_sandstone", "cut_red_sandstone"))
        trio("smooth_red_sandstone")
        trio("stone")
        trio("stone_bricks", "stone_brick", cuttingInputs = emptyList())
        // spacial naming 4
        vanilla {
            nullRecipe("stone_brick_slab_from_stone_bricks_stonecutting")
            nullRecipe("stone_brick_slab_from_stone_stonecutting")
            nullRecipe("stone_brick_stairs_from_stone_bricks_stonecutting")
            nullRecipe("stone_brick_stairs_from_stone_stonecutting")
            nullRecipe("stone_brick_wall_from_stone_bricks_stonecutting")
            nullRecipe("stone_brick_walls_from_stone_stonecutting")
        }
        trio("smooth_stone")
        trio("tuff")
        trio("polished_tuff", cuttingInputs = listOf("tuff", "polished_tuff"))
        trio("tuff_bricks", "tuff_brick", cuttingInputs = listOf("tuff", "polished_tuff", "tuff_bricks"))

        polish("andesite", "polished_andesite")
        polish("blackstone", "polished_blackstone")
        polish("polished_blackstone", "polished_blackstone_bricks",
            listOf("blackstone", "polished_blackstone"))
        polish("cobbled_deepslate", "polished_deepslate")
        polish("polished_deepslate", "deepslate_bricks",
            listOf("cobbled_deepslate", "polished_deepslate"))
        polish("deepslate_bricks", "deepslate_tiles",
            listOf("cobbled_deepslate", "polished_deepslate", "deepslate_bricks"))
        polish("diorite", "polished_diorite")
        polish("end_stone", "end_stone_bricks")
        polish("granite", "polished_granite")
        polish("packed_mud", "mud_bricks", cuttingInputs = emptyList())
        polish("popped_chorus_fruit", "purpur_block", emptyList())
        polish("prismarine", "prismarine_bricks", cuttingInputs = emptyList())
        polish("quartz_block", "quartz_bricks")
        polish("sand", "sandstone", emptyList())
        polish("sandstone", "cut_sandstone")
        polish("red_sand", "red_sandstone", emptyList())
        polish("red_sandstone", "cut_red_sandstone")
        polish("stone", "stone_bricks")
        polish("tuff", "polished_tuff")
        polish("polished_tuff", "tuff_bricks", listOf("tuff", "polished_tuff"))
        polish("basalt", "polished_basalt")

        chisel("cobbled_deepslate", "chiseled_deepslate")
        chisel("polished_blackstone", "chiseled_polished_blackstone",
            listOf("blackstone", "polished_blackstone"))
        chisel("nether_bricks", "chiseled_nether_bricks")
        chisel("quartz_block", "chiseled_quartz_block")
        chisel("sandstone", "chiseled_sandstone")
        chisel("red_sandstone", "chiseled_red_sandstone")
        chisel("stone_bricks", "chiseled_stone_bricks")
        vanilla {
            nullRecipe("chiseled_stone_bricks_stone_from_stonecutting")
        }
        chisel("tuff", "chiseled_tuff")
        chisel("tuff_bricks", "chiseled_tuff_bricks",
            listOf("tuff", "polished_tuff", "tuff_bricks"))

        polish("quartz_block", "quartz_pillar", lens = "topaz")
        polish("purpur_block", "purpur_pillar", lens = "topaz")

        coppers()
    }
}
