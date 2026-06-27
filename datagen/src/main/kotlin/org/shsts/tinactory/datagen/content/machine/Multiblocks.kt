package org.shsts.tinactory.datagen.content.machine

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.client.model.generators.ConfiguredModel
import net.minecraftforge.common.Tags
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.AllMultiblocks.getMultiblock
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.AllTags.CLEANROOM_DOOR
import org.shsts.tinactory.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.AllTags.COIL
import org.shsts.tinactory.AllTags.ELECTRIC_FURNACE
import org.shsts.tinactory.AllTags.FUSION_SHELL
import org.shsts.tinactory.AllTags.GLASS_CASING
import org.shsts.tinactory.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.AllTags.POWER_BLOCK
import org.shsts.tinactory.AllTags.machine
import org.shsts.tinactory.content.multiblock.TurbineBlock.CENTER_BLADE
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.ic2
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.core.util.LocHelper.suffix
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.cubeCasing
import org.shsts.tinactory.datagen.content.Models.cubeColumn
import org.shsts.tinactory.datagen.content.Models.multiblockInterface
import org.shsts.tinactory.datagen.content.Models.rotateModel
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.Models.turbineBlock
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.itemEntry
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.BlockDataFactory
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.arcFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assemblyLine
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.fusionReactor
import org.shsts.tinactory.datagen.content.builder.RecipeFactory
import org.shsts.tinactory.datagen.content.builder.SimpleAssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS
import org.shsts.tinactory.datagen.content.machine.Machines.machineModel
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS
import org.shsts.tinactory.integration.network.MachineBlock
import org.shsts.tinactory.integration.network.PrimitiveBlock
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder

object Multiblocks {
    private const val CASING_TICKS = 140L
    private const val COIL_TICKS = 200L
    private const val MULTIBLOCK_TICKS = 320L
    private const val ADVANCED_MULTIBLOCK_TICKS = 2400L

    fun init() {
        components()
        componentRecipes()
        machines()
        machineRecipes()
    }

    private fun components() {
        blockData {
            defaults {
                tag(MINEABLE_WITH_WRENCH)
            }

            for (entry in SOLID_CASINGS.values) {
                val tex = "casings/solid/machine_casing_${name(entry.id(), -1)}"
                block(entry) {
                    blockState { ctx ->
                        val existingHelper = ctx.provider().models().existingFileHelper
                        val texLoc = gregtech("blocks/$tex")
                        if (existingHelper.exists(texLoc, Models.TEXTURE_TYPE)) {
                            solidBlock(ctx, texLoc)
                        }
                    }
                }
            }

            block("multiblock/solid/insulated_battery") {
                blockState { ctx -> solidBlock(ctx, ic2("blocks/wiring/storage/mfe_bottomtop")) }
                noDrop()
            }

            block("multiblock/solid/reinforced_alloy") {
                blockState { ctx -> solidBlock(ctx, ic2("blocks/generator/reactor/reactor_vessel")) }
                noDrop()
            }

            coil("cupronickel")
            coil("kanthal")
            coil("nichrome")
            coil("tungsten", "rtm_alloy")
            coil("naquadah")

            misc("grate_machine_casing") {
                blockState(solidBlock("casings/pipe/grate_steel_front/top"))
            }

            misc("assembler_machine_casing") {
                blockState(cubeColumn("casings/mechanic/machine_casing_assembly_line",
                    "casings/solid/machine_casing_solid_steel"))
            }

            misc("autofarm_base") {
                blockState { ctx ->
                    val provider = ctx.provider()
                    provider.simpleBlock(ctx.`object`(), provider.models().cubeTop(
                        ctx.id(),
                        gregtech("blocks/casings/solid/machine_casing_solid_steel"),
                        mcLoc("block/farmland_moist")))
                }
            }

            misc("clear_glass") {
                blockState { ctx -> solidBlock(ctx, modLoc("blocks/multiblock/glass/quartz_glass_a")) }
                tag(CLEANROOM_WALL)
                tag(Tags.Blocks.GLASS)
            }

            misc("hardened_glass") {
                blockState(solidBlock("casings/transparent/tempered_glass"))
                tag(GLASS_CASING)
            }

            misc("plascrete") {
                blockState(solidBlock("casings/cleanroom/plascrete"))
                tag(CLEANROOM_WALL)
            }

            misc("filter_casing") {
                blockState(cubeColumn("casings/cleanroom/plascrete", "casings/cleanroom/filter_casing"))
            }

            misc("ptfe_pipe_casing") {
                blockState(solidBlock("casings/pipe/machine_casing_pipe_polytetrafluoroethylene"))
            }

            misc("launch_site_base") {
                blockState { ctx ->
                    val provider = ctx.provider()
                    val tex = gregtech("blocks/foam/reinforced_stone")
                    provider.simpleBlock(ctx.`object`(), provider.models().slab(
                        ctx.id(), tex, tex, tex))
                }
            }

            misc("lithography_lens/basic") {
                blockState(cubeCasing("casings/solid/machine_casing_solid_steel",
                    "overlay/machine/overlay_laser_target"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }

            misc("lithography_lens/good") {
                blockState(cubeCasing("casings/solid/machine_casing_stable_titanium",
                    "overlay/machine/overlay_laser_source"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }

            misc("lithography_lens/advanced") {
                blockState(cubeCasing("casings/solid/machine_casing_robust_tungstensteel",
                    "overlay/machine/overlay_laser_source"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }

            misc("lithography_lens/nether") {
                blockState(cubeCasing("casings/fusion/machine_casing_fusion",
                    "overlay/machine/overlay_laser_source"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }

            misc("metal_processing_chamber") {
                blockState(cubeColumn("casings/gearbox/machine_casing_gearbox_tungstensteel",
                    "casings/solid/machine_casing_robust_tungstensteel"))
            }

            misc("ore_processing_chamber") {
                blockState(cubeCasing("casings/solid/machine_casing_robust_tungstensteel",
                    "overlay/machine/overlay_filter"))
            }

            misc("precision_tooling_casing") {
                blockState(cubeColumn("casings/gearbox/machine_casing_gearbox_titanium",
                    "casings/solid/machine_casing_stable_titanium"))
            }

            misc("batch_rotor_casing") {
                blockState(cubeCasing("casings/solid/machine_casing_inert_ptfe",
                    "overlay/machine/overlay_filter"))
            }

            misc("phase_converter_casing") {
                blockState(cubeColumn("casings/solid/machine_casing_inert_ptfe",
                    "casings/pipe/machine_casing_pipe_polytetrafluoroethylene"))
            }

            misc("extrusion_die_casing") {
                blockState(cubeCasing("casings/solid/machine_casing_robust_tungstensteel",
                    "overlay/machine/overlay_pipe_out"))
            }

            misc("geological_sensor_casing") {
                blockState(cubeCasing("casings/solid/machine_casing_stable_titanium",
                    "cover/overlay_activity_detector"))
            }

            misc("electrode_casing") {
                blockState(cubeCasing("casings/solid/machine_casing_inert_ptfe",
                    "overlay/machine/overlay_energy_out"))
            }

            misc("turbine_blade") {
                blockState { ctx ->
                    turbineBlock(ctx, "casings/solid/machine_casing_stable_titanium",
                        modLoc("blocks/multiblock/large_turbine/idle"),
                        modLoc("blocks/multiblock/large_turbine/spin"))
                }
                itemModel(Models::turbineItem)
            }

            for (v in Voltage.between(Voltage.HV, Voltage.ZPM)) {
                val texName = if (v == Voltage.HV) "empty_tier_i" else "lapotronic_${v.id}"
                misc("power_block/${v.id}") {
                    blockState(cubeColumn("casings/battery/$texName"))
                    tag(POWER_BLOCK)
                }
            }

            misc("firebox_casing") {
                blockState { ctx ->
                    val provider = ctx.provider()
                    val models = provider.models()
                    val casing = gregtech("blocks/casings/solid/machine_casing_robust_tungstensteel")
                    val overlay = gregtech("blocks/casings/firebox/machine_casing_firebox_tungstensteel")
                    val working = suffix(overlay, "_active")
                    val baseModel = models.cubeColumn(ctx.id(), overlay, casing)
                    val workingModel = models.cubeColumn(ctx.id() + "_active", working, casing)
                    provider.getVariantBuilder(ctx.`object`()).forAllStates { state ->
                        val model = if (state.getValue(MachineBlock.WORKING)) workingModel else baseModel
                        ConfiguredModel.builder()
                            .modelFile(model)
                            .build()
                    }
                }
            }

            misc("nuclear_chamber") {
                blockState(cubeColumn(ic2("blocks/generator/reactor/reactor_chamber_sides"),
                    ic2("blocks/generator/reactor/reactor_chamber_top")))
            }

            misc("fusion_casing") {
                blockState(solidBlock("casings/fusion/machine_casing_fusion"))
                tag(FUSION_SHELL)
            }

            misc("fusion_glass") {
                blockState(solidBlock("casings/transparent/fusion_glass"))
                tag(FUSION_SHELL)
            }

            misc("superconducting_coil") {
                blockState(solidBlock("casings/fusion/machine_coil_superconductor"))
            }
        }

        dataGen {
            tag(BlockTags.DOORS, CLEANROOM_DOOR)
            tag(GLASS_CASING, CLEANROOM_WALL)
            tag(GLASS_CASING, Tags.Blocks.GLASS)
        }
    }

    private fun BlockDataFactory.coil(name: String, texName: String = name) {
        val entry = COIL_BLOCKS.getValue(name)
        val tex = "casings/coils/machine_coil_$texName"
        block(entry) {
            blockState(solidBlock(tex))
            tag(COIL)
        }
    }

    private fun BlockDataFactory.misc(name: String, block: IBlockDataBuilder<Block, *>.() -> Unit) {
        block("multiblock/misc/$name", block)
    }

    private fun componentRecipes() {
        assembler {
            solid("heatproof", Voltage.ULV, "invar", Technologies.STEEL)
            solid("solid_steel", Voltage.LV, "steel", Technologies.STEEL)
            solid("frost_proof", Voltage.LV, "aluminium", Technologies.VACUUM_FREEZER)
            solid("clean_stainless_steel", Voltage.MV, "stainless_steel", Technologies.DISTILLATION)
            solid("stable_titanium", Voltage.HV, "titanium", Technologies.ADVANCED_CHEMISTRY)
            solid("robust_tungstensteel", Voltage.EV, "tungsten_steel", Technologies.TUNGSTEN_STEEL)

            coil("cupronickel", Voltage.ULV, "cupronickel", "bronze", null, Technologies.STEEL)
            coil("kanthal", Voltage.LV, "kanthal", "silver", null, Technologies.KANTHAL)
            coil("nichrome", Voltage.MV, "nichrome", "stainless_steel", "pe", Technologies.NICHROME)
            coil("tungsten", Voltage.HV, "tungsten", "annealed_copper", "pe", Technologies.TUNGSTEN_STEEL)
            coil("naquadah", Voltage.IV, "naquadah", "hssg", "ptfe", Technologies.NAQUADAH_PROCESSING)
        }

        val itemFilter = getItem("component/item_filter")
        val advancedAlloy = getItem("component/advanced_alloy")

        assembler {
            defaults {
                voltage(Voltage.LV)
            }

            componentVoltage = Voltage.LV
            misc("grate_machine_casing", 2) {
                input("steel", "stick", 4)
                component("electric_motor")
                input("tin", "rotor")
                input(itemFilter, 6)
                input("soldering_alloy", amount = 2)
                workTicks(CASING_TICKS)
                tech(Technologies.SIFTING)
            }
            misc("autofarm_base") {
                input("steel", "stick", 2)
                input(Items.COARSE_DIRT, 2)
                input(Items.PODZOL, 2)
                input("steel", "plate", 3)
                input("soldering_alloy", amount = 2)
                workTicks(MACHINE_TICKS)
                tech(Technologies.AUTOFARM)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.MV)
                workTicks(MACHINE_TICKS)
            }

            componentVoltage = Voltage.MV
            misc("clear_glass") {
                input("steel", "stick", 2)
                input("glass", "primary")
                input("pe", amount = 3)
                tech(Technologies.ORGANIC_CHEMISTRY)
            }
            misc("plascrete") {
                input("steel", "stick", 2)
                input("pe", "sheet", 3)
                tech(Technologies.CLEANROOM)
            }
            misc("filter_casing", 2) {
                input("steel", "stick", 4)
                component("electric_motor")
                input("bronze", "rotor")
                input(itemFilter, 3)
                input("pe", "sheet", 3)
                input("soldering_alloy", amount = 2)
                tech(Technologies.CLEANROOM)
            }
            solid("inert_ptfe") {
                solid("solid_steel")
                input("ptfe", amount = 1.5)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
            misc("ptfe_pipe_casing") {
                input("steel", "stick", 2)
                input("ptfe", "pipe", 2)
                input("ptfe", "sheet", 2)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.HV)
            }

            componentVoltage = Voltage.HV
            misc("launch_site_base") {
                input("aluminium", "stick", 2)
                input(advancedAlloy, 2)
                input("soldering_alloy", amount = 1.5)
                workTicks(CASING_TICKS)
                tech(Technologies.ROCKET_SCIENCE)
            }

            componentVoltage = Voltage.EV
            misc("lithography_lens/basic") {
                input("titanium", "stick", 4)
                component("robot_arm", 2)
                input("ruby", "lens", 16)
                input("diamond", "lens", 16)
                input("sapphire", "lens", 16)
                input("emerald", "lens", 16)
                input("steel", "plate", 6)
                input("soldering_alloy", amount = 3)
                workTicks(MULTIBLOCK_TICKS)
                tech(Technologies.LITHOGRAPHY)
            }
            misc("turbine_blade") {
                solid("stable_titanium")
                pic(1)
                component("electric_motor", 4)
                input("stainless_steel", "rotor", 8)
                component("cable", 4)
                input("soldering_alloy", amount = 2)
                workTicks(MACHINE_TICKS)
                tech(Technologies.LARGE_TURBINE)
            }
        }

        arcFurnace {
            misc("hardened_glass") {
                misc("clear_glass")
                input("oxygen", amount = 0.6)
                voltage(Voltage.HV)
                workTicks(768)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.EV)
                workTicks(MULTIBLOCK_TICKS)
            }

            componentVoltage = Voltage.EV
            misc("metal_processing_chamber") {
                solid("robust_tungstensteel")
                circuit(2)
                component("electric_motor", 12)
                component("electric_piston", 4)
                component("cable", 16)
                input("soldering_alloy", amount = 2)
                tech(Technologies.METAL_FORMER)
            }
            solid("insulated_battery") {
                solid("frost_proof")
                input("battery_alloy", "plate", 3)
                input("soldering_alloy", amount = 2)
                tech(Technologies.POWER_SUBSTATION)
                workTicks(CASING_TICKS)
            }
            misc("firebox_casing") {
                solid("robust_tungstensteel")
                circuit(1)
                machine("electric_furnace")
                input(advancedAlloy, 4)
                input("soldering_alloy", amount = 2)
                tech(Technologies.LARGE_BOILER)
            }
            solid("reinforced_alloy") {
                solid("stable_titanium")
                input(advancedAlloy, 6)
                input("soldering_alloy", amount = 2)
                tech(Technologies.NUCLEAR_PHYSICS)
                workTicks(CASING_TICKS)
            }
            misc("nuclear_chamber") {
                solid("reinforced_alloy")
                circuit(2)
                component("robot_arm")
                component("electric_pump")
                input("lead", "plate", 24)
                input(advancedAlloy, 6)
                input("soldering_alloy", amount = 3)
                tech(Technologies.NUCLEAR_PHYSICS)
            }
            misc("ore_processing_chamber") {
                solid("robust_tungstensteel")
                circuit(2)
                component("electric_motor", 6)
                component("electric_piston", 2)
                input("annealed_copper", "wire", 32)
                component("grinder", 4)
                component("cable", 8)
                input("soldering_alloy", amount = 2)
                tech(Technologies.MINERAL_BENEFICIATION)
            }
            misc("precision_tooling_casing") {
                solid("stable_titanium")
                circuit(2)
                component("electric_motor", 6)
                component("electric_piston", 2)
                component("grinder", 4)
                component("buzzsaw", 4)
                component("cable", 8)
                input("soldering_alloy", amount = 2)
                tech(Technologies.METAL_FORMER)
            }
            misc("batch_rotor_casing") {
                solid("inert_ptfe")
                circuit(1)
                component("electric_motor", 16)
                component("cable", 16)
                input("soldering_alloy", amount = 2)
                tech(Technologies.MATERIAL_CONDITIONING)
            }

            componentVoltage = Voltage.IV
            misc("lithography_lens/good") {
                misc("lithography_lens/basic")
                component("robot_arm", 2)
                input("topaz", "lens", 16)
                input("blue_topaz", "lens", 16)
                input("titanium", "plate", 6)
                input("soldering_alloy", amount = 3)
                tech(Technologies.LITHOGRAPHY)
            }
            misc("phase_converter_casing") {
                misc("ptfe_pipe_casing")
                circuit(2)
                component("electric_pump", 8)
                component("electric_piston", 4)
                input("annealed_copper", "wire", 32)
                input("glass", "primary", 4)
                component("cable", 12)
                input("soldering_alloy", amount = 2)
                tech(Technologies.MATERIAL_CONDITIONING)
            }
            misc("extrusion_die_casing") {
                solid("robust_tungstensteel")
                circuit(1)
                component("electric_piston", 12)
                input("niobium_titanium", "wire", 32)
                input("ptfe", "pipe", 4)
                component("cable", 12)
                input("soldering_alloy", amount = 2)
                tech(Technologies.EXTRUSION_PRESS)
            }
            misc("geological_sensor_casing") {
                solid("stable_titanium")
                circuit(3)
                component("sensor", 8)
                component("electric_pump", 4)
                component("emitter", 4)
                component("cable", 16)
                input("soldering_alloy", amount = 2)
                tech(Technologies.PROSPECTING_STATION)
            }
            misc("electrode_casing") {
                solid("inert_ptfe")
                circuit(1)
                component("emitter", 4)
                input("platinum", "wire", 64)
                input("glass", "primary", 4)
                component("cable", 4)
                input("soldering_alloy", amount = 2)
                tech(Technologies.ELECTROCHEMICAL_PROCESSING)
            }
            misc("assembler_machine_casing", 2) {
                solid("solid_steel")
                circuit(3)
                component("robot_arm", 8)
                component("conveyor_module", 4)
                component("electric_motor", 4)
                input("tungsten_steel", "plate", 8)
                input("soldering_alloy", amount = 4)
                tech(Technologies.ASSEMBLY_LINE)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.IV)
                workTicks(MULTIBLOCK_TICKS)
            }

            componentVoltage = Voltage.IV
            misc("fusion_casing") {
                input("hssg", "stick", 2)
                input("rhodium_plated_palladium", "plate", 3)
                input("ruridit", "foil", 8)
                input("soldering_alloy", amount = 2)
                workTicks(CASING_TICKS)
                tech(Technologies.FUSION)
            }
            misc("fusion_glass") {
                misc("hardened_glass", 2)
                input("hssg", "stick", 2)
                input("rhodium_plated_palladium", "plate", 3)
                input("ruridit", "foil", 8)
                input("soldering_alloy", amount = 3)
                workTicks(CASING_TICKS)
                tech(Technologies.FUSION)
            }
            misc("superconducting_coil") {
                input("iv_superconductor", "wire", 32)
                input("ruridit", "foil", 32)
                input("rhodium_plated_palladium", "plate", 2)
                input("ptfe", amount = 2)
                input("soldering_alloy", amount = 4)
                workTicks(COIL_TICKS)
                tech(Technologies.FUSION)
            }
            misc("superconducting_coil", suffix = "_from_luv_superconductor") {
                input("luv_superconductor", "wire", 4)
                input("ruridit", "foil", 4)
                input("rhodium_plated_palladium", "plate", 2)
                input("ptfe", amount = 2)
                input("soldering_alloy", amount = 4)
                workTicks(COIL_TICKS)
                tech(Technologies.FUSION)
            }
            misc("lithography_lens/advanced") {
                misc("lithography_lens/good")
                component("robot_arm", 2)
                input("ender_eye", "lens", 16)
                input("hssg", "plate", 6)
                input("soldering_alloy", amount = 3)
                tech(Technologies.ENDER_CHEMISTRY)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.LUV)
                workTicks(MULTIBLOCK_TICKS)
            }

            componentVoltage = Voltage.LUV
            misc("lithography_lens/nether") {
                misc("lithography_lens/advanced")
                component("robot_arm", 2)
                input("nether_star", "lens", 16)
                input("hssg", "stick", 4)
                input("rhodium_plated_palladium", "plate", 6)
                input("ruridit", "foil", 16)
                input("soldering_alloy", amount = 4)
                tech(Technologies.ADVANCED_NETHER_CHEMISTRY)
            }
        }

        powerBlocks()
    }

    private fun powerBlocks() {
        assembler {
            defaults {
                input("soldering_alloy", amount = 1.5)
                workTicks(COIL_TICKS)
                tech(Technologies.POWER_SUBSTATION)
            }

            powerBlock(Voltage.HV) {
                input("battery_powder", "dust", 10)
            }
            powerBlock(Voltage.EV, "energy_crystal", 10)
            powerBlock(Voltage.IV, "lapotron_crystal", 8)
            powerBlock(Voltage.LUV, "lapotronic_energy_orb")
        }
    }

    private fun AssemblyRecipeFactory.powerBlock(v: Voltage,
        component: String? = null, amount: Int = 1,
        block: SimpleAssemblyRecipeBuilder.() -> Unit = {}) {
        componentVoltage = v
        misc("power_block/${v.id}") {
            input("aluminium", "stick", 2)
            input("battery_alloy", "plate", 3)
            pic(2)
            component("cable", 4)
            if (component != null) {
                input(getItem("component/$component"), amount)
            }
            voltage(v)
            block()
        }
    }

    private fun AssemblyRecipeFactory.solid(name: String, block: SimpleAssemblyRecipeBuilder.() -> Unit) {
        output(SOLID_CASINGS.getValue(name).get(), block = block)
    }

    private fun ProcessingRecipeBuilder<*, *>.solid(name: String) {
        input(SOLID_CASINGS.getValue(name).get())
    }

    private fun <R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>> RecipeFactory<R, B>.misc(
        name: String, amount: Int = 1, suffix: String = "", block: B.() -> Unit) {
        output(getItem("multiblock/misc/$name"), amount, suffix = suffix, block = block)
    }

    private fun ProcessingRecipeBuilder<*, *>.misc(
        name: String, amount: Int = 1) {
        input(getItem("multiblock/misc/$name"), amount)
    }

    private fun AssemblyRecipeFactory.solid(name: String,
        v: Voltage, mat: String, tech: ResourceLocation) {
        solid(name) {
            input(mat, "stick", 2)
            input(mat, "plate", 3)
            if (v != Voltage.ULV) {
                input("soldering_alloy", amount = 2)
            }
            voltage(v)
            workTicks(CASING_TICKS)
            tech(tech)
        }
    }

    private fun AssemblyRecipeFactory.coil(name: String,
        v: Voltage, wire: String, foil: String, insulation: String?, tech: ResourceLocation) {
        val amount = 8 * v.rank
        val block = COIL_BLOCKS.getValue(name).get()
        output(block) {
            input(wire, "wire", amount)
            input(foil, "foil", amount)
            if (insulation != null) {
                input(insulation, amount = 2)
            }
            voltage(v)
            workTicks(COIL_TICKS)
            tech(tech)
        }
    }

    private fun machines() {
        blockData {
            defaults {
                tag(MINEABLE_WITH_WRENCH)
            }
            for (entry in getMachine("multiblock/interface").entries()) {
                block(entry) {
                    blockState { ctx ->
                        multiblockInterface(ctx, IO_TEX)
                    }
                }
            }
            for (entry in getMachine("multiblock/digital_interface").entries()) {
                block(entry) {
                    blockState { ctx ->
                        multiblockInterface(ctx, ME_BUS)
                    }
                }
            }
            multiblock("blast_furnace", "heatproof")
            multiblock("sifter", "solid_steel", "blast_furnace")
            multiblock("autofarm", "solid_steel", "blast_furnace")
            multiblock("vacuum_freezer", "frost_proof")
            multiblock("distillation_tower", "clean_stainless_steel")
            block("multiblock/cleanroom") {
                machineModel {
                    casing("casings/cleanroom/plascrete")
                    overlay("multiblock/cleanroom")
                }
            }
            multiblock("oil_cracking_unit", "clean_stainless_steel", "blast_furnace")
            multiblock("pyrolyse_oven", "heatproof")
            multiblock("large_chemical_reactor", "inert_ptfe")
            multiblock("implosion_compressor", "solid_steel")
            multiblock("autoclave", "clean_stainless_steel", "blast_furnace")
            multiblock("lithography_machine", "stable_titanium", "blast_furnace")
            multiblock("rocket_launch_site", "solid_steel", "blast_furnace")
            multiblock("multi_smelter", "heatproof", "blast_furnace")
            dataGen {
                tag(itemEntry("multiblock/multi_smelter"), ELECTRIC_FURNACE)
            }
            multiblock("metal_former", "frost_proof", "blast_furnace")
            multiblock("ore_processing_unit", "solid_steel", "blast_furnace")
            multiblock("precision_cutting_machine", "stable_titanium", "blast_furnace")
            multiblock("batching_vessel", "clean_stainless_steel", "blast_furnace")
            multiblock("phase_exchange_chamber", "inert_ptfe", "blast_furnace")
            multiblock("extrusion_press", "robust_tungstensteel", "blast_furnace")
            multiblock("prospecting_station", "stable_titanium", "blast_furnace")
            multiblock("electrochemical_processor", "stable_titanium", "blast_furnace")
            getMultiblock("large_turbine").apply {
                block(block) {
                    blockState { ctx ->
                        val prov = ctx.provider()
                        val models = prov.models()
                        val id = ctx.id()
                        val modelId = "block/multiblock/misc/turbine_blade_$CENTER_BLADE"
                        val idle = models.withExistingParent(id, modLoc(modelId))
                        val spin = models.withExistingParent("${id}_active", modLoc("${modelId}_active"))
                        prov.getVariantBuilder(ctx.`object`()).forAllStates { state ->
                            val dir = state.getValue(PrimitiveBlock.FACING)
                            val model = if (state.getValue(
                                    MachineBlock.WORKING)) spin else idle
                            rotateModel(model, dir)
                        }
                    }
                    for (type in types) {
                        itemTag(machine(type))
                    }
                }
            }
            multiblock("power_substation", ic2("blocks/wiring/storage/mfe_bottomtop"),
                gregtech("blocks/multiblock/power_substation"))
            multiblock("large_boiler", "robust_tungstensteel", "blast_furnace")
            multiblock("nuclear_reactor", ic2("blocks/generator/reactor/reactor_vessel"),
                modLoc("blocks/multiblock/nuclear_reactor"))
            multiblock("assembly_line", "solid_steel", "blast_furnace")
            multiblock("fusion_reactor", gregtech("blocks/casings/fusion/machine_casing_fusion"),
                gregtech("blocks/multiblock/fusion_reactor"))
        }
    }

    private fun BlockDataFactory.multiblock(name: String, casing: ResourceLocation, overlay: ResourceLocation) {
        val set = getMultiblock(name)
        block(set.block) {
            machineModel {
                casing(casing)
                overlay(overlay)
            }
            for (type in set.types) {
                itemTag(machine(type))
            }
        }
    }

    private fun BlockDataFactory.multiblock(name: String, casing: String, overlay: String = name) {
        multiblock(name, gregtech("blocks/casings/solid/machine_casing_$casing"),
            gregtech("blocks/multiblock/$overlay"))
    }

    private fun machineRecipes() {
        val itemFilter = getItem("component/item_filter")
        val advancedAlloy = getItem("component/advanced_alloy")

        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            multiblock("blast_furnace") {
                solid("heatproof")
                machine("electric_furnace", 3)
                circuit(3)
                component("cable", 2)
                tech(Technologies.STEEL)
            }
        }

        assembler {
            componentVoltage = Voltage.LV
            defaults {
                voltage(Voltage.LV)
                workTicks(MACHINE_TICKS)
            }
            multiblock("sifter") {
                solid("solid_steel")
                circuit(3, Voltage.MV)
                component("electric_piston", 4)
                component("cable", 4)
                input(itemFilter, 4)
                input("steel", "plate", 4)
                tech(Technologies.SIFTING)
            }
            multiblock("autofarm") {
                misc("autofarm_base")
                circuit(4, Voltage.MV)
                component("electric_pump", 4)
                component("cable", 4)
                input("brass", "pipe", 4)
                input("steel", "plate", 4)
                tech(Technologies.AUTOFARM)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.MV)
                workTicks(MACHINE_TICKS)
            }

            componentVoltage = Voltage.MV
            multiblock("vacuum_freezer") {
                solid("frost_proof")
                circuit(3, Voltage.HV)
                component("electric_pump", 4)
                component("cable", 4)
                input("aluminium", "plate", 4)
                tech(Technologies.VACUUM_FREEZER)
            }
            multiblock("pyrolyse_oven") {
                solid("heatproof")
                circuit(3)
                component("electric_piston", 2)
                component("electric_pump", 2)
                component("cable", 4)
                input("cupronickel", "wire", 16)
                input("invar", "plate", 4)
                tech(Technologies.PYROLYSE_OVEN)
            }

            componentVoltage = Voltage.HV
            multiblock("distillation_tower") {
                solid("clean_stainless_steel")
                circuit(4)
                component("electric_pump", 2)
                component("cable", 4)
                input("stainless_steel", "pipe", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.DISTILLATION)
            }
            multiblock("oil_cracking_unit") {
                solid("clean_stainless_steel")
                circuit(3)
                component("electric_pump", 2)
                component("electric_piston", 2)
                component("cable", 4)
                input("stainless_steel", "pipe", 4)
                tech(Technologies.OIL_CRACKING)
            }
            multiblock("cleanroom") {
                misc("plascrete")
                circuit(3)
                component("electric_motor", 2)
                component("cable", 4)
                input(itemFilter, 4)
                input("pe", "sheet", 4)
                tech(Technologies.CLEANROOM)
            }
            multiblock("large_chemical_reactor") {
                solid("inert_ptfe")
                circuit(4)
                component("electric_motor", 4)
                input("stainless_steel", "rotor", 4)
                component("cable", 4)
                input("ptfe", "pipe", 4)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.HV)
                workTicks(MACHINE_TICKS)
            }

            componentVoltage = Voltage.HV
            multiblock("implosion_compressor") {
                solid("solid_steel")
                circuit(3)
                component("electric_piston", 4)
                component("cable", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.TNT)
            }
            multiblock("autoclave") {
                solid("clean_stainless_steel")
                circuit(3, Voltage.EV)
                component("electric_motor", 2)
                component("electric_pump", 2)
                component("cable", 4)
                input("stainless_steel", "rotor", 4)
                tech(Technologies.AUTOCLAVE)
            }
            multiblock("rocket_launch_site") {
                solid("solid_steel")
                circuit(4)
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 4)
                input(advancedAlloy, 4)
                tech(Technologies.ROCKET_SCIENCE)
            }

            componentVoltage = Voltage.EV
            multiblock("lithography_machine") {
                solid("stable_titanium")
                circuit(3, Voltage.IV)
                component("emitter", 4, Voltage.HV)
                component("conveyor_module", 4)
                component("cable", 4)
                misc("lithography_lens/basic")
                tech(Technologies.LITHOGRAPHY)
            }
            multiblock("multi_smelter") {
                solid("heatproof")
                circuit(3)
                machine("electric_furnace")
                machine("alloy_smelter")
                machine("arc_furnace")
                component("conveyor_module", 4)
                component("cable", 4)
                tech(Technologies.MULTI_SMELTER)
            }
            multiblock("large_turbine") {
                misc("turbine_blade")
                circuit(4)
                pic(4)
                machine("steam_turbine", voltage = Voltage.HV)
                machine("gas_turbine", voltage = Voltage.HV)
                machine("combustion_generator", voltage = Voltage.HV)
                component("electric_pump", 4)
                component("cable", 16)
                tech(Technologies.LARGE_TURBINE)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.EV)
                workTicks(MACHINE_TICKS)
            }

            componentVoltage = Voltage.EV
            multiblock("metal_former") {
                solid("frost_proof")
                circuit(3)
                machine("bender")
                machine("wiremill")
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 4)
                tech(Technologies.METAL_FORMER)
            }
            multiblock("power_substation") {
                solid("insulated_battery")
                circuit(3)
                pic(6)
                component("electric_pump", 2)
                component("cable", 16)
                tech(Technologies.POWER_SUBSTATION)
            }
            multiblock("large_boiler") {
                misc("firebox_casing")
                circuit(2)
                component("conveyor_module", 4)
                component("electric_pump", 4)
                component("cable", 4)
                input(advancedAlloy, 8)
                tech(Technologies.LARGE_BOILER)
            }
            multiblock("nuclear_reactor") {
                misc("nuclear_chamber")
                circuit(4)
                component("field_generator", 4)
                component("electric_pump", 4)
                component("cable", 8)
                input(advancedAlloy, 12)
                tech(Technologies.NUCLEAR_PHYSICS)
            }

            componentVoltage = Voltage.IV
            multiblock("ore_processing_unit") {
                solid("solid_steel")
                circuit(4)
                machine("macerator")
                machine("ore_washer")
                machine("thermal_centrifuge")
                component("robot_arm", 4)
                component("electric_pump", 4)
                component("cable", 8)
                tech(Technologies.MINERAL_BENEFICIATION)
            }
            multiblock("precision_cutting_machine") {
                solid("stable_titanium")
                circuit(3)
                machine("cutter")
                machine("lathe")
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 8)
                tech(Technologies.METAL_FORMER)
            }
            multiblock("batching_vessel") {
                solid("clean_stainless_steel")
                circuit(3)
                machine("mixer")
                machine("centrifuge")
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 8)
                tech(Technologies.MATERIAL_CONDITIONING)
            }
            multiblock("assembly_line") {
                misc("assembler_machine_casing")
                circuit(3, Voltage.LUV)
                machine("assembler")
                machine("circuit_assembler")
                component("robot_arm", 8)
                component("conveyor_module", 8)
                component("cable", 16)
                tech(Technologies.ASSEMBLY_LINE)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.IV)
                workTicks(MACHINE_TICKS)
            }

            componentVoltage = Voltage.IV
            multiblock("phase_exchange_chamber") {
                solid("inert_ptfe")
                circuit(3)
                machine("extractor")
                machine("fluid_solidifier")
                component("robot_arm", 4)
                component("electric_pump", 4)
                component("cable", 8)
                tech(Technologies.MATERIAL_CONDITIONING)
            }
            multiblock("extrusion_press") {
                solid("robust_tungstensteel")
                circuit(2)
                machine("extruder")
                component("robot_arm", 4)
                component("electric_piston", 4)
                component("cable", 8)
                tech(Technologies.EXTRUSION_PRESS)
            }
            multiblock("prospecting_station") {
                solid("stable_titanium")
                circuit(3)
                machine("ore_analyzer")
                machine("stone_generator")
                component("emitter", 4)
                component("conveyor_module", 4)
                component("cable", 8)
                tech(Technologies.PROSPECTING_STATION)
            }
            multiblock("electrochemical_processor") {
                solid("stable_titanium")
                circuit(3)
                machine("polarizer")
                machine("electrolyzer")
                component("robot_arm", 4)
                component("electric_pump", 4)
                component("cable", 8)
                tech(Technologies.ELECTROCHEMICAL_PROCESSING)
            }
        }

        assemblyLine {
            componentVoltage = Voltage.LUV
            multiblock("fusion_reactor") {
                component("machine_hull")
                component("robot_arm", 4)
                component("electric_pump", 4)
                component("field_generator", 8, Voltage.IV)
                circuit(8)
                input("hssg", "stick", 8)
                input("ruridit", "foil", 32)
                input("soldering_alloy", amount = 16)
                voltage(Voltage.IV)
                workTicks(ADVANCED_MULTIBLOCK_TICKS)
                tech(Technologies.FUSION)
            }
        }

        fusionReactor {
            recipe("multiblock/netherite") {
                input("netherite_scrap", "molten")
                input("gold", "molten")
                output("netherite", "plasma", 0.25)
                voltage(Voltage.LUV)
                workTicks(400)
            }
            recipe("multiblock/nether_star") {
                input("wither_matrix", "liquid")
                input("enriched_naquadah", "molten")
                output("nether_star", "plasma")
                voltage(Voltage.LUV)
                workTicks(800)
            }
            recipe("multiblock/activated_naquadah") {
                input("naquadah", "molten")
                input("hydrogen", "gas", 0.125)
                output("activated_naquadah", "plasma")
                voltage(Voltage.LUV)
                workTicks(400)
            }
            recipe("multiblock/fusion_reactor_smoke") {
                input("water", "liquid", 1)
                input("water", "gas", 1)
                output("water", "gas", 2)
                voltage(Voltage.LUV)
                workTicks(200)
            }
        }
    }

    private fun AssemblyRecipeFactory.multiblock(name: String, block: SimpleAssemblyRecipeBuilder.() -> Unit) {
        output(getMultiblock(name).block.get(), block = block)
    }
}
