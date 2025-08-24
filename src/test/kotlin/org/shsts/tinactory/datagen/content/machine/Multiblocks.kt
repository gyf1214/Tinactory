package org.shsts.tinactory.datagen.content.machine

import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE
import org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS
import org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS
import org.shsts.tinactory.content.AllMultiblocks.GRATE_MACHINE_CASING
import org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS
import org.shsts.tinactory.content.AllTags.CLEANROOM_WALL
import org.shsts.tinactory.content.AllTags.COIL
import org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.mcLoc
import org.shsts.tinactory.core.util.LocHelper.name
import org.shsts.tinactory.datagen.content.Models.solidBlock
import org.shsts.tinactory.datagen.content.builder.DataFactories.block

object Multiblocks {
    fun init() {
        components()
    }

    private fun components() {
        for (entry in SOLID_CASINGS) {
            val tex = "casings/solid/machine_casing_${name(entry.id(), -1)}"
            block(entry) {
                blockState(solidBlock(tex))
                tag(MINEABLE_WITH_WRENCH)
            }
        }

        for (entry in COIL_BLOCKS) {
            val tex = "casings/coils/machine_coil_${name(entry.id(), -1)}"
            block(entry) {
                blockState(solidBlock(tex))
                tag(COIL)
                tag(MINEABLE_WITH_WRENCH)
            }
        }

        block(GRATE_MACHINE_CASING) {
            blockState(solidBlock("casings/pipe/grate_steel_front/top"))
            tag(MINEABLE_WITH_WRENCH)
        }

        block(AUTOFARM_BASE) {
            blockState { ctx ->
                val provider = ctx.provider()
                val model = provider.models().cubeTop(ctx.id(),
                    gregtech("blocks/casings/solid/machine_casing_solid_steel"),
                    mcLoc("block/farmland_moist"))
                provider.simpleBlock(ctx.`object`(), model)
            }
            tag(MINEABLE_WITH_WRENCH)
        }

        block(CLEAR_GLASS) {
            blockState(solidBlock("casings/transparent/fusion_glass"))
            tag(MINEABLE_WITH_WRENCH)
            tag(CLEANROOM_WALL)
            tag(Tags.Blocks.GLASS)
        }
    }
}
