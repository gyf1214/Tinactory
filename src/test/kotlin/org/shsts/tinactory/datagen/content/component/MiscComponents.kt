package org.shsts.tinactory.datagen.content.component

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.FLUID_CELL
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla

object MiscComponents {
    fun init() {
        vanilla {
            shapeless(getMaterial("iron").tag("wire"),
                CABLE.item(Voltage.ULV),
                fromAmount = 4, criteria = "has_wire")
        }

        toolCrafting(MACHINE_HULL.item(Voltage.ULV)) {
            pattern("###")
            pattern("#W#")
            pattern("###")
            define('#', getMaterial("iron").tag("plate"))
            define('W', CABLE.getValue(Voltage.ULV))
            toolTag(TOOL_WRENCH)
        }

        toolCrafting(FLUID_CELL.item(Voltage.ULV)) {
            pattern("###")
            pattern("#G#")
            pattern(" #")
            define('#', getMaterial("iron").tag("plate"))
            define('G', getMaterial("glass").tag("primary"))
            toolTag(TOOL_HAMMER, TOOL_WRENCH)
        }

        assembler {
            defaults {
                tech(Technologies.SOLDERING)
            }
            output(FLUID_CELL.item(Voltage.ULV)) {
                input("iron", "plate", 4)
                input("glass", "primary")
                input("soldering_alloy")
                voltage(Voltage.ULV)
                workTicks(100)
            }
            output(Items.NAME_TAG) {
                input("iron", "plate")
                input(TOOL_HANDLE)
                voltage(Voltage.LV)
                workTicks(64)
            }
        }
    }
}
