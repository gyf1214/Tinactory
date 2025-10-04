package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.material.MaterialSet
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.builder.TechBuilder.RANK_PER_VOLTAGE
import org.shsts.tinactory.datagen.content.Technologies.BASE_ORE
import org.shsts.tinactory.datagen.content.Technologies.TECHS
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

class VeinBuilder(private val id: String, private val rank: Int, private val rate: Double) {
    var primitive = false
    var baseOre = false
    private var variant: OreVariant? = null
    private var block: ProcessingRecipeBuilder<OreAnalyzerRecipe.Builder>.() -> Unit = {}
    private val ores = mutableListOf<MaterialSet>()

    companion object {
        const val VEIN_TECH_RANK = RANK_PER_VOLTAGE / 2
    }

    private fun chain(another: ProcessingRecipeBuilder<OreAnalyzerRecipe.Builder>.() -> Unit) {
        val oldBlock = block
        block = {
            oldBlock()
            another()
        }
    }

    fun variant(value: OreVariant) {
        variant = value
        chain {
            input(value.baseItem)
        }
    }

    fun ore(name: String, rate: Double) {
        val mat = getMaterial(name)
        if (variant == null) {
            variant(mat.oreVariant())
        }
        chain {
            if (mat.hasItem("raw")) {
                output(mat, "raw", rate = rate)
            } else {
                output(mat, "raw_fluid", rate = rate)
            }
        }
        ores.add(mat)
    }

    fun build() {
        val variant1 = variant!!
        assert(rate > 0)
        assert(ores.isNotEmpty())
        val id1 = "${variant1.serializedName}/$id"

        val baseTech = BASE_ORE.getValue(variant1)
        val tech = if (baseOre || primitive) {
            baseTech
        } else {
            TECHS.builder("ore/$id1") { handler, parent, loc ->
                TechBuilder.factory(handler, parent, loc)
            }.run {
                maxProgress(30)
                val mat = ores[0]
                if (mat.hasItem("raw")) {
                    displayItem(mat.item("raw"))
                } else {
                    displayItem(mat.item("raw_fluid"))
                }
                depends(baseTech)
                researchVoltage(variant1.voltage)
                rank(rank + 1 + VEIN_TECH_RANK)
                register()
            }
        }

        val builder = run {
            val recipeType = REGISTRATE.getRecipeType<OreAnalyzerRecipe.Builder>("ore_analyzer")
            val builder = recipeType.recipe(DATA_GEN, id1).apply {
                rate(this@VeinBuilder.rate)
            }
            ProcessingRecipeBuilder(builder).apply {
                simpleDefaults()
                amperage = 0.125
                workTicks(128)
            }
        }

        builder.apply(block)
        if (primitive) {
            builder.voltage(Voltage.PRIMITIVE)
        } else {
            builder.voltage(variant1.voltage)
            builder.extra {
                requireTech(tech)
            }
        }
        builder.build()
    }
}
