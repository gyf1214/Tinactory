package org.shsts.tinactory.datagen.content.machine

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR
import org.shsts.tinactory.content.AllItems.ITEM_FILTER
import org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE
import org.shsts.tinactory.content.AllMultiblocks.CLEAN_STAINLESS_CASING
import org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS
import org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.content.AllMultiblocks.CUPRONICKEL_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.FILTER_CASING
import org.shsts.tinactory.content.AllMultiblocks.FROST_PROOF_CASING
import org.shsts.tinactory.content.AllMultiblocks.GRATE_MACHINE_CASING
import org.shsts.tinactory.content.AllMultiblocks.HEATPROOF_CASING
import org.shsts.tinactory.content.AllMultiblocks.INERT_PTFE_CASING
import org.shsts.tinactory.content.AllMultiblocks.KANTHAL_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.NICHROME_COIL_BLOCK
import org.shsts.tinactory.content.AllMultiblocks.PLASCRETE
import org.shsts.tinactory.content.AllMultiblocks.PTFE_PIPE_CASING
import org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.content.AllMultiblocks.SOLID_STEEL_CASING
import org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR
import org.shsts.tinactory.content.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.content.AllTags.COIL
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinycorelib.api.registrate.entry.IEntry

object Multiblocks {
    fun init() {
        components()
        componentRecipes()
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

            for (entry in COIL_BLOCKS) {
                val tex = "casings/coils/machine_coil_${name(entry.id(), -1)}"
                block(entry) {
                    blockState(solidBlock(tex))
                    tag(COIL)
                }
            }

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
        }

        dataGen {
            tag(BlockTags.DOORS, CLEANROOM_DOOR)
        }
    }

    private fun componentRecipes() {
        assembler {
            solidRecipe(HEATPROOF_CASING, Voltage.ULV, "invar", Technologies.STEEL)
            solidRecipe(SOLID_STEEL_CASING, Voltage.LV, "steel", Technologies.STEEL)
            solidRecipe(FROST_PROOF_CASING, Voltage.LV, "aluminium", Technologies.VACUUM_FREEZER)
            solidRecipe(CLEAN_STAINLESS_CASING, Voltage.MV, "stainless_steel", Technologies.DISTILLATION)

            coilRecipe(CUPRONICKEL_COIL_BLOCK, Voltage.ULV, "cupronickel", "bronze", Technologies.STEEL)
            coilRecipe(KANTHAL_COIL_BLOCK, Voltage.LV, "kanthal", "silver", Technologies.KANTHAL)
            coilRecipe(NICHROME_COIL_BLOCK, Voltage.MV, "nichrome", "stainless_steel", Technologies.NICHROME)
        }

        assembler {
            componentVoltage = Voltage.LV
            defaults {
                voltage(Voltage.LV)
            }
            output(GRATE_MACHINE_CASING.get(), 2) {
                input("steel", "stick", 4)
                input(ELECTRIC_MOTOR)
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
                input(ELECTRIC_MOTOR)
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
    }

    private fun AssemblyRecipeFactory.solidRecipe(block: IEntry<out Block>,
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

    private fun AssemblyRecipeFactory.coilRecipe(block: IEntry<out Block>,
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
}
