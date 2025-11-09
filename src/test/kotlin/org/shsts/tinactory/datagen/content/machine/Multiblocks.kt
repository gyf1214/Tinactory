package org.shsts.tinactory.datagen.content.machine

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.content.AllMultiblocks.getMultiblock
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR
import org.shsts.tinactory.content.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.content.AllTags.COIL
import org.shsts.tinactory.content.AllTags.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllTags.GLASS_CASING
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.content.AllTags.POWER_BLOCK
import org.shsts.tinactory.content.AllTags.machine
import org.shsts.tinactory.content.multiblock.TurbineBlock.CENTER_BLADE
import org.shsts.tinactory.content.network.MachineBlock
import org.shsts.tinactory.content.network.PrimitiveBlock
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.ic2
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.cubeCasing
import org.shsts.tinactory.datagen.content.Models.cubeColumn
import org.shsts.tinactory.datagen.content.Models.multiblockInterface
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.Models.turbineBlock
import org.shsts.tinactory.datagen.content.RegistryHelper.getBlock
import org.shsts.tinactory.datagen.content.RegistryHelper.getItem
import org.shsts.tinactory.datagen.content.RegistryHelper.itemEntry
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.BlockDataFactory
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.ProcessingRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.arcFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactory
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS
import org.shsts.tinactory.datagen.content.machine.Machines.machineModel
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder

object Multiblocks {
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
                        if (existingHelper.exists(gregtech("blocks/$tex"), Models.TEXTURE_TYPE)) {
                            solidBlock(ctx, tex)
                        }
                    }
                }
            }

            block("multiblock/solid/insulated_battery") {
                blockState { ctx ->
                    val model = ctx.provider().models()
                        .cubeAll(ctx.id(), ic2("blocks/wiring/storage/mfe_bottomtop"))
                    ctx.provider().simpleBlock(ctx.`object`(), model)
                }
            }

            coil("cupronickel")
            coil("kanthal")
            coil("nichrome")
            coil("tungsten", "rtm_alloy")

            misc("grate_machine_casing") {
                blockState(solidBlock("casings/pipe/grate_steel_front/top"))
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
                blockState(solidBlock("casings/transparent/fusion_glass"))
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

            misc("metal_processing_chamber") {
                blockState(cubeColumn("casings/gearbox/machine_casing_gearbox_tungstensteel",
                    "casings/solid/machine_casing_robust_tungstensteel"))
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

            coil("cupronickel", Voltage.ULV, "cupronickel", "bronze", Technologies.STEEL)
            coil("kanthal", Voltage.LV, "kanthal", "silver", Technologies.KANTHAL)
            coil("nichrome", Voltage.MV, "nichrome", "stainless_steel", Technologies.NICHROME)
            coil("tungsten", Voltage.HV, "tungsten", "annealed_copper", Technologies.TUNGSTEN_STEEL)
        }

        val itemFilter = getItem("component/item_filter")

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
                workTicks(140)
                tech(Technologies.SIFTING)
            }
            misc("autofarm_base") {
                input("steel", "stick", 2)
                input(Items.COARSE_DIRT, 2)
                input(Items.PODZOL, 2)
                input("steel", "plate", 3)
                input("soldering_alloy", amount = 2)
                workTicks(200)
                tech(Technologies.AUTOFARM)
            }
        }

        assembler {
            defaults {
                voltage(Voltage.MV)
                workTicks(200)
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
                input(getItem("component/advanced_alloy"), 2)
                input("soldering_alloy", amount = 1.5)
                workTicks(140)
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
                workTicks(320)
                tech(Technologies.LITHOGRAPHY)
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
                workTicks(320)
            }

            componentVoltage = Voltage.EV
            misc("metal_processing_chamber") {
                solid("robust_tungstensteel")
                circuit(2)
                component("electric_motor", 8)
                component("electric_piston", 4)
                component("robot_arm", 2)
                component("conveyor_module", 2)
                component("cable", 16)
                input("soldering_alloy", amount = 2)
                tech(Technologies.METAL_FORMER)
            }
            solid("insulated_battery") {
                solid("frost_proof")
                input("battery_alloy", "plate", 3)
                input("soldering_alloy", amount = 2)
                tech(Technologies.POWER_SUBSTATION)
                workTicks(140)
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
        }

        assembler {
            defaults {
                input("aluminium", "stick", 2)
                input("battery_alloy", "plate", 3)
                pic(2)
                component("cable", 4)
                input("soldering_alloy", amount = 1.5)
                workTicks(200)
                tech(Technologies.POWER_SUBSTATION)
            }

            componentVoltage = Voltage.HV
            misc("power_block/hv") {
                input("battery_powder", "dust", 10)
                voltage(Voltage.HV)
            }
        }
    }

    private fun AssemblyRecipeFactory.solid(name: String, block: AssemblyRecipeBuilder.() -> Unit) {
        output(SOLID_CASINGS.getValue(name).get(), block = block)
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>> ProcessingRecipeBuilder<B>.solid(name: String) {
        input(SOLID_CASINGS.getValue(name).get())
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>,
        RB : ProcessingRecipeBuilder<B>> RecipeFactory<B, RB>.misc(
        name: String, amount: Int = 1, block: RB.() -> Unit) {
        output(getBlock("multiblock/misc/$name"), amount, block = block)
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>> ProcessingRecipeBuilder<B>.misc(name: String) {
        input(getBlock("multiblock/misc/$name"))
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
            workTicks(140)
            tech(tech)
        }
    }

    private fun AssemblyRecipeFactory.coil(name: String,
        v: Voltage, wire: String, foil: String, tech: ResourceLocation) {
        val amount = 8 * v.rank
        val block = COIL_BLOCKS.getValue(name).get()
        output(block) {
            input(wire, "wire", amount)
            input(foil, "foil", amount)
            if (v.rank >= Voltage.MV.rank) {
                input("pe", amount = 2)
            }
            voltage(v)
            workTicks(200)
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
            getMultiblock("large_turbine").apply {
                block(block) {
                    blockState { ctx ->
                        val prov = ctx.provider()
                        val models = prov.models()
                        val id = ctx.id()
                        val modelId = "block/multiblock/misc/turbine_blade_$CENTER_BLADE"
                        val idle = models.withExistingParent(id, modLoc(modelId))
                        val spin = models.withExistingParent("${id}_active", modLoc("${modelId}_active"))
                        prov.getVariantBuilder(ctx.`object`())
                            .forAllStates { state ->
                                val dir = state.getValue(PrimitiveBlock.FACING)
                                val model = if (state.getValue(MachineBlock.WORKING)) spin else idle
                                Models.rotateModel(model, dir)
                            }
                    }
                    for (type in types) {
                        itemTag(machine(type))
                    }
                }
            }
            block("multiblock/power_substation") {
                machineModel {
                    casing(ic2("blocks/wiring/storage/mfe_bottomtop"))
                    overlay("multiblock/power_substation")
                }
            }
        }
    }

    private fun BlockDataFactory.multiblock(name: String, casing: String, overlay: String = name) {
        val set = getMultiblock(name)
        block(set.block) {
            machineModel {
                casing("casings/solid/machine_casing_$casing")
                overlay("multiblock/$overlay")
            }
            for (type in set.types) {
                itemTag(machine(type))
            }
        }
    }

    private fun machineRecipes() {
        val itemFilter = getItem("component/item_filter")

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
            multiblock("rocket_launch_site") {
                solid("solid_steel")
                circuit(4)
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 4)
                input(getItem("component/advanced_alloy"), 4)
                tech(Technologies.ROCKET_SCIENCE)
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
        }
    }

    private fun AssemblyRecipeFactory.multiblock(name: String, block: AssemblyRecipeBuilder.() -> Unit) {
        output(getMultiblock(name).block.get(), block = block)
    }
}
