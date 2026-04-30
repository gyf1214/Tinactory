package org.shsts.tinactory.datagen.content.recovery

import org.shsts.tinactory.AllMaterials.getMaterial

object MaterialRecoveryRecipes {
    fun init() {
        RecoveryRegistry.configure(
            targetSub = "ingot",
            lossRate = 0.9,
            subFactors = mapOf(
                "ingot" to 1.0,
                "dust" to 1.0,
                "plate" to 1.0,
                "wire" to 0.5,
                "stick" to 0.5,
                "ring" to 0.25,
                "screw" to 0.125,
                "foil" to 0.25,
                "bolt" to 0.125,
                "nugget" to 1.0 / 9.0,
                "gear" to 4.0,
                "rotor" to 4.0,
                "pipe" to 1.0),
            materialMap = mapOf(
                getMaterial("copper") to getMaterial("annealed_copper"),
                getMaterial("iron") to getMaterial("wrought_iron")),
            secondOutputRatio = 0.25,
            workTicksPerIngot = 128L,
            oxygenPerIngot = 0.1)
        RecoveryRegistry.emitArcFurnaceRecipes()
    }
}
