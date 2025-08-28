package org.shsts.tinactory.datagen.content.material

import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.datagen.content.builder.VeinBuilder

object Veins {
    fun init() {
        stone()
        deepslate()
        netherrack()
    }

    private fun stone() {
        vein("chalcopyrite", 0.4) {
            primitive = true
            ore("chalcopyrite", 0.8)
            ore("pyrite", 0.6)
        }
        vein("limonite", 0.6) {
            baseOre = true
            ore("limonite", 0.8)
            ore("banded_iron", 0.4)
            ore("garnierite", 0.2)
        }
        vein("coal", 0.3) {
            ore("coal", 1.0)
            ore("coal", 0.4)
        }
        vein("cassiterite", 0.2) {
            ore("tin", 1.0)
            ore("cassiterite", 0.3)
            ore("tin", 0.1)
        }
        vein("redstone", 0.1) {
            ore("redstone", 1.0)
            ore("ruby", 0.3)
            ore("cinnabar", 0.1)
        }
    }

    private fun deepslate() {
        vein("magnetite", 0.4) {
            ore("magnetite", 1.0)
            ore("gold", 0.2)
            ore("magnetite", 0.2)
        }
        vein("sulfide", 0.4) {
            ore("galena", 0.5)
            ore("sphalerite", 0.6)
            ore("silver", 0.3)
        }
        vein("graphite", 0.2) {
            ore("graphite", 1.0)
            ore("graphite", 0.3)
            ore("diamond", 0.1)
        }
        vein("bauxite", 0.5) {
            ore("bauxite", 0.8)
            ore("ilmenite", 0.6)
        }
        vein("oil", 0.3) {
            ore("natural_gas", 0.3)
            ore("light_oil", 0.4)
            ore("heavy_oil", 0.7)
        }
        vein("gem", 0.1) {
            ore("emerald", 0.4)
            ore("sapphire", 0.7)
        }
    }

    private fun netherrack() {
        vein("gold", 0.2) {
            variant(OreVariant.NETHERRACK)
            ore("gold", 0.5)
            ore("topaz", 0.3)
            ore("blue_topaz", 0.3)
        }
    }

    private var rank = 0

    private fun vein(id: String, rate: Double, block: VeinBuilder.() -> Unit) {
        VeinBuilder(id, rank++, rate).apply {
            block()
            build()
        }
    }
}
