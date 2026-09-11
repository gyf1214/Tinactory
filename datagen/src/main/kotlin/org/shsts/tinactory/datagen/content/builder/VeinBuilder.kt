package org.shsts.tinactory.datagen.content.builder

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.Level
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.AllWorldGens
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape
import org.shsts.tinactory.core.worldgen.ore.OreEntry
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.builder.TechBuilder.Companion.RANK_PER_VOLTAGE
import org.shsts.tinactory.datagen.content.Technologies.BASE_ORE
import org.shsts.tinactory.datagen.content.Technologies.TECHS
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.oreAnalyzer
import org.shsts.tinactory.datagen.content.material.Veins.VEIN_DATA
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinactory.integration.material.OreVariant

class VeinBuilder(private val id: String, private val rank: Int, private val rate: Double) {
    var primitive = false
    var baseOre = false
    private var variant: OreVariant? = null
    private var dimension: ResourceKey<Level>? = null
    private var minY: Int? = null
    private var maxY: Int? = null
    private var block: OreAnalyzerRecipeBuilder.() -> Unit = {}
    private val ores = mutableListOf<MaterialSet>()
    private val oreEntries = mutableListOf<OreEntry>()
    private var weightMultiple = 10

    companion object {
        const val VEIN_TECH_RANK = RANK_PER_VOLTAGE / 2

        private val ORE_SHAPE = OreShapeDefinition(
            AllWorldGens.ELLIPSOID_SHAPE.get(),
            EllipsoidShape.Definition(100.0, 200.0, 0.6, 2.0, 5.0))

        private val BIOME_TAGS = mapOf(
            Level.OVERWORLD to TagKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld")),
            Level.NETHER to TagKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_nether")),
            Level.END to TagKey.create(
                Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_end")))
    }

    private fun chain(another: OreAnalyzerRecipeBuilder.() -> Unit) {
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

    fun dimension(value: ResourceKey<Level>) {
        dimension = value
    }

    fun yRange(minY: Int, maxY: Int) {
        require(minY <= maxY) { "minY must not exceed maxY" }
        this.minY = minY
        this.maxY = maxY
    }

    fun ore(name: String, rate: Double) {
        val mat = getMaterial(name)
        if (variant == null) {
            variant(mat.oreMain())
        }
        val hostVariant = variant!!
        check(mat.hasOre(hostVariant)) {
            "Material $name does not have an ore for host variant ${hostVariant.serializedName}"
        }
        val oreBlock = mat.oreEntry(hostVariant).get()
        chain {
            if (mat.hasItem("raw")) {
                output(mat, "raw", rate = rate)
            } else {
                output(mat, "raw_fluid", rate = rate)
            }
        }
        ores.add(mat)
        oreEntries.add(OreEntry(oreBlock, rate))
    }

    fun build() {
        val variant1 = checkNotNull(variant) { "Vein $id must specify a host variant" }
        val dimension1 = checkNotNull(dimension) { "Vein $id must specify a dimension" }
        val biomeTag1 = BIOME_TAGS[dimension1] ?: error(
            "Vein $id has no biome tag for dimension ${dimension1.location()}")
        val minY1 = checkNotNull(minY) { "Vein $id must specify a minimum Y" }
        val maxY1 = checkNotNull(maxY) { "Vein $id must specify a maximum Y" }
        check(rate > 0) { "Vein $id must have a positive selection weight" }
        check(ores.isNotEmpty()) { "Vein $id must contain at least one ore" }
        val id1 = "${variant1.serializedName}/$id"

        val baseTech = BASE_ORE.getValue(variant1)
        val tech = if (baseOre || primitive) {
            baseTech
        } else {
            TECHS.builder("ore/$id1", TechBuilder.Companion::factory).run {
                maxProgress(10)
                val mat = ores[0]
                if (mat.hasItem("raw")) {
                    displayItem(mat.item("raw"))
                } else {
                    displayItem(mat.item("raw_fluid"))
                }
                depends(baseTech)
                researchVoltage(variant1.voltage)
                rank(rank + 1 + VEIN_TECH_RANK)
                build()
            }
        }

        oreAnalyzer {
            recipe(id1) {
                block()
                rate(rate)
                if (primitive) {
                    voltage(Voltage.PRIMITIVE)
                } else {
                    voltage(variant1.voltage)
                    requireTech(tech)
                }
            }
        }

        val weight = (weightMultiple * rate).toInt()
        val definition = OreVeinDefinition(
            minY1,
            maxY1,
            ORE_SHAPE,
            0.5,
            variant1.baseBlock,
            oreEntries.toList())
        VEIN_DATA.addCallback { provider ->
            provider.addVein(variant1.serializedName, weight, id1, biomeTag1, definition)
        }
    }
}
