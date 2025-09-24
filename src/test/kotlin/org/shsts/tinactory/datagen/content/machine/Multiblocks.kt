package org.shsts.tinactory.datagen.content.machine

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.MULTIBLOCK_INTERFACE
import org.shsts.tinactory.content.AllItems.ADVANCED_ALLOY
import org.shsts.tinactory.content.AllItems.ITEM_FILTER
import org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE
import org.shsts.tinactory.content.AllMultiblocks.BASIC_LITHOGRAPHY_LENS
import org.shsts.tinactory.content.AllMultiblocks.CLEANROOM
import org.shsts.tinactory.content.AllMultiblocks.CLEAN_STAINLESS_CASING
import org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS
import org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.content.AllMultiblocks.CUPRONICKEL_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.FILTER_CASING
import org.shsts.tinactory.content.AllMultiblocks.FROST_PROOF_CASING
import org.shsts.tinactory.content.AllMultiblocks.GOOD_LITHOGRAPHY_LENS
import org.shsts.tinactory.content.AllMultiblocks.GRATE_MACHINE_CASING
import org.shsts.tinactory.content.AllMultiblocks.HEATPROOF_CASING
import org.shsts.tinactory.content.AllMultiblocks.INERT_PTFE_CASING
import org.shsts.tinactory.content.AllMultiblocks.KANTHAL_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.LAUNCH_SITE_BASE
import org.shsts.tinactory.content.AllMultiblocks.NICHROME_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.PLASCRETE
import org.shsts.tinactory.content.AllMultiblocks.PTFE_PIPE_CASING
import org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.content.AllMultiblocks.SOLID_STEEL_CASING
import org.shsts.tinactory.content.AllMultiblocks.STABLE_TITANIUM_CASING
import org.shsts.tinactory.content.AllMultiblocks.TUNGSTEN_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.getMultiblock
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR
import org.shsts.tinactory.content.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.content.AllTags.COIL
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.content.AllTags.machine
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models.multiblockInterface
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.BlockDataFactory
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS
import org.shsts.tinactory.datagen.content.machine.Machines.machineModel
import org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX
import org.shsts.tinycorelib.api.registrate.entry.IEntry

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

            for (entry in SOLID_CASINGS) {
                val tex = "casings/solid/machine_casing_${name(entry.id(), -1)}"
                block(entry) {
                    blockState(solidBlock(tex))
                }
            }

            coil("cupronickel")
            coil("kanthal")
            coil("nichrome")
            coil("tungsten", "rtm_alloy")

            block(GRATE_MACHINE_CASING) {
                blockState(solidBlock("casings/pipe/grate_steel_front/top"))
            }

            block(AUTOFARM_BASE) {
                blockState { ctx ->
                    val provider = ctx.provider()
                    provider.simpleBlock(ctx.`object`(), provider.models().cubeTop(
                        ctx.id(),
                        gregtech("blocks/casings/solid/machine_casing_solid_steel"),
                        mcLoc("block/farmland_moist")))
                }
            }

            block(CLEAR_GLASS) {
                blockState(solidBlock("casings/transparent/fusion_glass"))
                tag(CLEANROOM_WALL)
                tag(Tags.Blocks.GLASS)
            }

            block(PLASCRETE) {
                blockState(solidBlock("casings/cleanroom/plascrete"))
                tag(CLEANROOM_WALL)
            }

            block(FILTER_CASING) {
                blockState { ctx ->
                    val provider = ctx.provider()
                    provider.simpleBlock(ctx.`object`(), provider.models().cubeColumn(
                        ctx.id(),
                        gregtech("blocks/casings/cleanroom/plascrete"),
                        gregtech("blocks/casings/cleanroom/filter_casing")))
                }
            }

            block(PTFE_PIPE_CASING) {
                blockState(solidBlock("casings/pipe/machine_casing_pipe_polytetrafluoroethylene"))
            }

            block(LAUNCH_SITE_BASE) {
                blockState { ctx ->
                    val provider = ctx.provider()
                    val tex = gregtech("blocks/foam/reinforced_stone")
                    provider.simpleBlock(ctx.`object`(), provider.models().slab(
                        ctx.id(), tex, tex, tex))
                }
            }

            block(BASIC_LITHOGRAPHY_LENS) {
                blockState(solidBlock("casings/transparent/cleanroom_glass"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }

            block(GOOD_LITHOGRAPHY_LENS) {
                blockState(solidBlock("casings/transparent/laminated_glass"))
                tag(AllTags.LITHOGRAPHY_LENS)
            }
        }

        dataGen {
            tag(BlockTags.DOORS, CLEANROOM_DOOR)
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

    private fun componentRecipes() {
        assembler {
            solid(HEATPROOF_CASING, Voltage.ULV, "invar", Technologies.STEEL)
            solid(SOLID_STEEL_CASING, Voltage.LV, "steel", Technologies.STEEL)
            solid(FROST_PROOF_CASING, Voltage.LV, "aluminium", Technologies.VACUUM_FREEZER)
            solid(CLEAN_STAINLESS_CASING, Voltage.MV, "stainless_steel", Technologies.DISTILLATION)
            solid(STABLE_TITANIUM_CASING, Voltage.HV, "titanium", Technologies.ADVANCED_CHEMISTRY)

            coil(CUPRONICKEL_COIL_BLOCK, Voltage.ULV, "cupronickel", "bronze", Technologies.STEEL)
            coil(KANTHAL_COIL_BLOCK, Voltage.LV, "kanthal", "silver", Technologies.KANTHAL)
            coil(NICHROME_COIL_BLOCK, Voltage.MV, "nichrome", "stainless_steel", Technologies.NICHROME)
            coil(TUNGSTEN_COIL_BLOCK, Voltage.HV, "tungsten", "platinum", Technologies.HYDROMETALLURGY)
        }

        assembler {
            componentVoltage = Voltage.LV
            defaults {
                voltage(Voltage.LV)
            }
            output(GRATE_MACHINE_CASING.get(), 2) {
                input("steel", "stick", 4)
                component("electric_motor")
                input("tin", "rotor")
                input(ITEM_FILTER.get(), 6)
                input("soldering_alloy", amount = 2)
                workTicks(140)
                tech(Technologies.SIFTING)
            }
            output(AUTOFARM_BASE.get()) {
                input("steel", "stick", 2)
                input(Blocks.COARSE_DIRT, 2)
                input(Blocks.PODZOL, 2)
                input("steel", "plate", 3)
                input("soldering_alloy", amount = 2)
                workTicks(200)
                tech(Technologies.AUTOFARM)
            }
        }

        assembler {
            componentVoltage = Voltage.MV
            defaults {
                voltage(Voltage.MV)
                workTicks(200)
            }
            output(CLEAR_GLASS.get()) {
                input("steel", "stick", 2)
                input("glass", "primary")
                input("pe", amount = 3)
                tech(Technologies.ORGANIC_CHEMISTRY)
            }
            output(PLASCRETE.get()) {
                input("steel", "stick", 2)
                input("pe", "sheet", 3)
                tech(Technologies.CLEANROOM)
            }
            output(FILTER_CASING.get(), 2) {
                input("steel", "stick", 4)
                component("electric_motor")
                input("bronze", "rotor")
                input(ITEM_FILTER.get(), 3)
                input("pe", "sheet", 3)
                input("soldering_alloy", amount = 2)
                tech(Technologies.CLEANROOM)
            }
            output(INERT_PTFE_CASING.get()) {
                input(SOLID_STEEL_CASING.get())
                input("ptfe", amount = 1.5)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
            output(PTFE_PIPE_CASING.get()) {
                input("steel", "stick", 2)
                input("ptfe", "pipe", 2)
                input("ptfe", "sheet", 2)
                tech(Technologies.ADVANCED_CHEMISTRY)
            }
        }

        assembler {
            componentVoltage = Voltage.HV
            defaults {
                voltage(Voltage.HV)
            }

            output(LAUNCH_SITE_BASE.get()) {
                input("aluminium", "stick", 2)
                input(ADVANCED_ALLOY.get(), 2)
                input("soldering_alloy", amount = 1.5)
                workTicks(140)
                tech(Technologies.ROCKET_SCIENCE)
            }
            output(BASIC_LITHOGRAPHY_LENS.get()) {
                input("titanium", "stick", 4)
                component("robot_arm", 2, voltage = Voltage.EV)
                input("ruby", "lens", 4)
                input("diamond", "lens", 4)
                input("sapphire", "lens", 4)
                input("emerald", "lens", 4)
                input("soldering_alloy", amount = 3)
                workTicks(320)
                tech(Technologies.LITHOGRAPHY)
            }
        }

        assembler {
            output(GOOD_LITHOGRAPHY_LENS.get()) {
                input(BASIC_LITHOGRAPHY_LENS.get())
                component("robot_arm", 2, voltage = Voltage.IV)
                input("topaz", "lens", 4)
                input("blue_topaz", "lens", 4)
                input("soldering_alloy", amount = 3)
                workTicks(320)
                voltage(Voltage.EV)
                tech(Technologies.LITHOGRAPHY)
            }
        }
    }

    private fun AssemblyRecipeFactory.solid(block: IEntry<out Block>,
        v: Voltage, mat: String, tech: ResourceLocation) {
        output(block.get()) {
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

    private fun AssemblyRecipeFactory.coil(block: IEntry<out Block>,
        v: Voltage, wire: String, foil: String, tech: ResourceLocation) {
        val amount = 8 * v.rank
        output(block.get()) {
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
            for (entry in MULTIBLOCK_INTERFACE.values) {
                block(entry) {
                    blockState(multiblockInterface(IO_TEX))
                }
            }
            multiblock("blast_furnace", "heatproof")
            multiblock("sifter", "solid_steel", "blast_furnace")
            multiblock("autofarm", "solid_steel", "blast_furnace")
            multiblock("vacuum_freezer", "frost_proof")
            multiblock("distillation_tower", "clean_stainless_steel")
            block(CLEANROOM) {
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
        }
    }

    private fun BlockDataFactory.multiblock(type: String, casing: String, overlay: String = type) {
        val set = getMultiblock(type)
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
        assembler {
            componentVoltage = Voltage.ULV
            defaults {
                voltage(Voltage.ULV)
                workTicks(MACHINE_TICKS)
            }
            multiblock("blast_furnace") {
                input(HEATPROOF_CASING.get())
                input(ELECTRIC_FURNACE, 3)
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
                input(SOLID_STEEL_CASING.get())
                circuit(3, Voltage.MV)
                component("electric_piston", 4)
                component("cable", 4)
                input(ITEM_FILTER.get(), 4)
                input("steel", "plate", 4)
                tech(Technologies.SIFTING)
            }
            multiblock("autofarm") {
                input(AUTOFARM_BASE.get())
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
                input(FROST_PROOF_CASING.get())
                circuit(3, Voltage.HV)
                component("electric_pump", 4)
                component("cable", 4)
                input("aluminium", "plate", 4)
                tech(Technologies.VACUUM_FREEZER)
            }
            multiblock("pyrolyse_oven") {
                input(HEATPROOF_CASING.get())
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
                input(CLEAN_STAINLESS_CASING.get())
                circuit(4)
                component("electric_pump", 2)
                component("cable", 4)
                input("stainless_steel", "pipe", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.DISTILLATION)
            }
            multiblock("oil_cracking_unit") {
                input(CLEAN_STAINLESS_CASING.get())
                circuit(3)
                component("electric_pump", 2)
                component("electric_piston", 2)
                component("cable", 4)
                input("stainless_steel", "pipe", 4)
                tech(Technologies.OIL_CRACKING)
            }
            output(CLEANROOM.get()) {
                input(PLASCRETE.get())
                circuit(3)
                component("electric_motor", 2)
                component("cable", 4)
                input(ITEM_FILTER.get(), 4)
                input("pe", "sheet", 4)
                tech(Technologies.CLEANROOM)
            }
            multiblock("large_chemical_reactor") {
                input(INERT_PTFE_CASING.get())
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
                input(SOLID_STEEL_CASING.get())
                circuit(3)
                component("electric_piston", 4)
                component("cable", 4)
                input("stainless_steel", "plate", 4)
                tech(Technologies.TNT)
            }
            multiblock("autoclave") {
                input(CLEAN_STAINLESS_CASING.get())
                circuit(3, Voltage.EV)
                component("electric_motor", 2)
                component("electric_pump", 2)
                component("cable", 4)
                input("stainless_steel", "rotor", 4)
                tech(Technologies.AUTOCLAVE)
            }

            componentVoltage = Voltage.EV
            multiblock("lithography_machine") {
                input(STABLE_TITANIUM_CASING.get())
                circuit(3, Voltage.IV)
                component("emitter", 4, Voltage.HV)
                component("conveyor_module", 4)
                component("cable", 4)
                input(BASIC_LITHOGRAPHY_LENS.get())
                tech(Technologies.LITHOGRAPHY)
            }
            multiblock("rocket_launch_site") {
                input(SOLID_STEEL_CASING.get())
                circuit(4)
                component("robot_arm", 4)
                component("conveyor_module", 4)
                component("cable", 4)
                input(ADVANCED_ALLOY.get(), 4)
                tech(Technologies.ROCKET_SCIENCE)
            }
        }
    }

    private fun AssemblyRecipeFactory.multiblock(name: String, block: AssemblyRecipeBuilder.() -> Unit) {
        output(getMultiblock(name).block.get(), block = block)
    }
}
