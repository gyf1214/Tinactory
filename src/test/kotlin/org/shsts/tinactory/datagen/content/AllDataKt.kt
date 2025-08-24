package org.shsts.tinactory.datagen.content

import org.shsts.tinactory.datagen.content.chemistry.InorganicChemistry
import org.shsts.tinactory.datagen.content.chemistry.OilProcessing
import org.shsts.tinactory.datagen.content.chemistry.OrganicChemistry
import org.shsts.tinactory.datagen.content.component.Components
import org.shsts.tinactory.datagen.content.component.MiscComponents
import org.shsts.tinactory.datagen.content.material.Crops
import org.shsts.tinactory.datagen.content.material.Generators
import org.shsts.tinactory.datagen.content.material.Materials
import org.shsts.tinactory.datagen.content.material.MiscMaterials
import org.shsts.tinactory.datagen.content.material.Veins
import org.shsts.tinactory.datagen.content.material.Woods

object AllDataKt {
    fun init() {
        Technologies.init()
        Materials.init()
        Woods.init()
        Crops.init()
        Generators.init()
        MiscMaterials.init()

        InorganicChemistry.init()
        OrganicChemistry.init()
        OilProcessing.init()

        Components.init()
        MiscComponents.init()

        Veins.init()
        Markers.init()
    }
}
