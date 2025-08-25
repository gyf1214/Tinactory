package org.shsts.tinactory.datagen.content.machine

import net.minecraft.tags.BlockTags
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE
import org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS
import org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.content.AllMultiblocks.FILTER_CASING
import org.shsts.tinactory.content.AllMultiblocks.GRATE_MACHINE_CASING
import org.shsts.tinactory.content.AllMultiblocks.PLASCRETE
import org.shsts.tinactory.content.AllMultiblocks.PTFE_PIPE_CASING
import org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR
import org.shsts.tinactory.content.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.content.AllTags.COIL
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen

object Multiblocks {
    fun init() {
        components()
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
}
