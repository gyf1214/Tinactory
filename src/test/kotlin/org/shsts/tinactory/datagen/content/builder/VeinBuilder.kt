package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.MaterialSet
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.builder.TechBuilder.RANK_PER_VOLTAGE
import org.shsts.tinactory.datagen.content.Technologies.BASE_ORE
import org.shsts.tinactory.datagen.content.Technologies.TECHS
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

class VeinBuilder(private val id: String, private val rank: Int, private val rate: Double) {
    var primitive = false
    var baseOre = false
    private var variant: OreVariant? = null
    private val builder = run {
        val recipeType = REGISTRATE.getRecipeType<OreAnalyzerRecipe.Builder>("ore_analyzer")
        val builder = recipeType.recipe(DATA_GEN, id).apply {
            rate(this@VeinBuilder.rate)
        }
        ProcessingRecipeBuilder(builder).apply {
            simpleDefaults()
            amperage = 0.125
            workTicks(32)
        }
    }
    private val ores = mutableListOf<MaterialSet>()

    companion object {
        const val VEIN_TECH_RANK = RANK_PER_VOLTAGE / 2
    }

    fun variant(value: OreVariant) {
        variant = value
        builder.input(value.baseItem)
    }

    fun ore(name: String, rate: Double) {
        val mat = getMaterial(name)
        builder.output(mat, "raw", rate = rate)
        ores.add(mat)
        if (variant == null) {
            variant(mat.oreVariant())
        }
    }

    fun build() {
        val variant1 = variant!!
        assert(rate > 0)
        assert(ores.isNotEmpty())

        val baseTech = BASE_ORE[variant1]
        val tech = if (baseOre || primitive) {
            baseTech
        } else {
            TECHS.builder("ore/$id") { handler, parent, loc ->
                TechBuilder.factory(handler, parent, loc)
            }.run {
                maxProgress(30)
                displayItem(ores[0].loc("raw"))
                depends(baseTech)
                researchVoltage(variant1.voltage)
                rank(rank + 1 + VEIN_TECH_RANK)
                register()
            }
        }

        if (primitive) {
            builder.voltage(Voltage.PRIMITIVE)
        } else {
            builder.voltage(variant1.voltage)
            builder.extra {
                requireTech(tech)
            }
        }
    }
}
