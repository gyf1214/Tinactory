package org.shsts.tinactory.datagen.content

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.chemistry.InorganicChemistry
import org.shsts.tinactory.datagen.content.chemistry.OilProcessing
import org.shsts.tinactory.datagen.content.chemistry.OrganicChemistry
import org.shsts.tinactory.datagen.content.material.Crops
import org.shsts.tinactory.datagen.content.material.Generators
import org.shsts.tinactory.datagen.content.material.Materials
import org.shsts.tinactory.datagen.content.material.MiscMaterials
import org.shsts.tinactory.datagen.content.material.Woods

object AllDataKt {
    fun init() {
        Materials.init()
        Woods.init()
        Crops.init()
        Generators.init()
        MiscMaterials.init()

        InorganicChemistry.init()
        OrganicChemistry.init()
        OilProcessing.init()

        assembler {
            output(Items.NAME_TAG) {
                input("iron", "plate")
                input(TOOL_HANDLE)
                voltage(Voltage.LV)
                workTicks(64)
                tech(Technologies.SOLDERING)
            }
        }
    }
}
