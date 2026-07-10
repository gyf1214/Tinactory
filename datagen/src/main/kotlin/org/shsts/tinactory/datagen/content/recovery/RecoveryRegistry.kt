package org.shsts.tinactory.datagen.content.recovery

import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.util.LocHelper.prepend
import org.shsts.tinactory.datagen.content.RegistryHelper.itemLoc
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.arcFurnace
import org.shsts.tinactory.integration.material.MaterialSet
import kotlin.math.floor

object RecoveryRegistry {
    private val LOGGER = LogUtils.getLogger()

    private data class Config(
        val targetSub: String,
        val recoverRate: Double,
        val subFactors: Map<String, Double>,
        val materialMap: Map<MaterialSet, MaterialSet>,
        val secondOutputRatio: Double,
        val workTicksPerIngot: Long,
        val oxygenPerIngot: Double,
        val maxRecoveredMaterialAmount: Int)

    private lateinit var config: Config
    val targetSub: String get() = config.targetSub
    private val recipesByItem = mutableMapOf<ResourceLocation, MutableList<RecoveryRecipe>>()
    private val selectedRecipeByItem = mutableMapOf<ResourceLocation, RecoveryRecipe?>()
    private val compositionByItem = mutableMapOf<ResourceLocation, RecoveryComposition>()
    private val compositionByRecipe = mutableMapOf<ResourceLocation, RecoveryComposition>()
    private val visitingItems = mutableSetOf<ResourceLocation>()
    private val visitingRecipes = mutableSetOf<ResourceLocation>()

    fun configure(
        targetSub: String,
        recoverRate: Double,
        subFactors: Map<String, Double>,
        materialMap: Map<MaterialSet, MaterialSet>,
        secondOutputRatio: Double,
        workTicksPerIngot: Long,
        oxygenPerIngot: Double,
        maxRecoveredMaterialAmount: Int = 64) {
        require(maxRecoveredMaterialAmount > 0)
        config = Config(
            targetSub,
            recoverRate,
            subFactors.toMap(),
            materialMap.toMap(),
            secondOutputRatio,
            workTicksPerIngot,
            oxygenPerIngot,
            maxRecoveredMaterialAmount)
        clearResolved()
    }

    fun record(key: ResourceLocation, output: RecoveryOutput, inputs: List<RecoveryInput>) {
        val recipe = RecoveryRecipe(key, output, inputs.toList())
        val loc = itemLoc(output.item)
        recipesByItem.getOrPut(loc) { mutableListOf() }.add(recipe)
    }

    fun emitArcFurnaceRecipes() {
        for (loc in recipesByItem.keys.sortedBy { it.toString() }) {
            if (!isTopLevelTarget(loc)) {
                continue
            }
            val composition = compositionOf(loc)
            val outputs = recoveryOutputs(composition)
            if (outputs.isEmpty()) {
                continue
            }
            val recipe = selectedRecipeByItem[loc] ?: continue
            val totalOutputAmount = outputs.sumOf { it.second }
            arcFurnace {
                recipe(prepend(loc, "recovery")) {
                    voltage(recipe.output.voltage ?: Voltage.HV)
                    workTicks(totalOutputAmount * config.workTicksPerIngot)
                    input(recipe.output.item)
                    input("oxygen", amount = totalOutputAmount * config.oxygenPerIngot)
                    for ((material, amount) in outputs) {
                        output(material, targetSub, amount, rate = config.recoverRate)
                    }
                }
            }
        }
    }

    fun compositionOf(item: ItemLike): RecoveryComposition {
        return compositionOf(itemLoc(item))
    }

    private fun compositionOf(loc: ResourceLocation): RecoveryComposition {
        compositionByItem[loc]?.let { return it }
        if (!visitingItems.add(loc)) {
            LOGGER.trace("Skipping cyclic recovery item input {}", loc)
            return RecoveryComposition()
        }
        val selected = selectRecipe(loc, recipesByItem[loc].orEmpty())
        selectedRecipeByItem[loc] = selected
        val composition = selected?.let { compositionOf(it) } ?: RecoveryComposition()
        compositionByItem[loc] = composition
        visitingItems.remove(loc)
        return composition
    }

    private fun compositionOf(recipe: RecoveryRecipe): RecoveryComposition {
        compositionByRecipe[recipe.key]?.let { return it }
        if (!visitingRecipes.add(recipe.key)) {
            LOGGER.trace("Skipping cyclic recovery recipe input {}", itemLoc(recipe.output.item))
            return RecoveryComposition()
        }
        var ret = RecoveryComposition()
        for (input in recipe.inputs) {
            val composition = when (input) {
                is RecoveryMaterialInput -> convert(input)
                    ?.let { RecoveryComposition(mapOf(it.first to it.second)) }
                    ?: RecoveryComposition()
                is RecoveryItemInput -> compositionOf(input.item).scale(input.amount)
            }
            ret = ret.plus(composition)
        }
        ret = ret.scale(1.0 / recipe.output.amount)
        compositionByRecipe[recipe.key] = ret
        visitingRecipes.remove(recipe.key)
        return ret
    }

    private fun selectRecipe(loc: ResourceLocation, candidates: List<RecoveryRecipe>): RecoveryRecipe? {
        val nonEmpty = candidates
            .mapNotNull {
                val composition = compositionOf(it)
                if (composition.isEmpty()) null else it to composition
            }
        if (nonEmpty.isEmpty()) {
            return null
        }
        val selected = nonEmpty.minWith(compareBy<Pair<RecoveryRecipe, RecoveryComposition>> {
            it.second.topMaterials(1).first().second
        }.thenBy {
            it.first.key.toString()
        }).first
        if (nonEmpty.size > 1) {
            val discarded = nonEmpty.map { it.first }.filter { it != selected }
            if (discarded.isNotEmpty()) {
                LOGGER.trace("Selected recovery recipe {} for {}", selected.key, loc)
            }
        }
        return selected
    }

    private fun convert(input: RecoveryMaterialInput): Pair<MaterialSet, Double>? {
        val factor = config.subFactors[input.sub] ?: return null
        val mapped = config.materialMap[input.material] ?: input.material
        if (!mapped.hasItem(targetSub)) {
            return null
        }
        return mapped to input.amount * factor
    }

    private fun recoveryOutputs(composition: RecoveryComposition): List<Pair<MaterialSet, Int>> {
        val top = composition.topMaterials(2)
        if (top.isEmpty()) {
            return listOf()
        }
        val ret = mutableListOf<Pair<MaterialSet, Int>>()
        val firstAmount = capRecoveredAmount(top[0].second)
        if (firstAmount <= 0) {
            return listOf()
        }
        ret += top[0].first to firstAmount
        if (top.size > 1 && top[1].second >= firstAmount * config.secondOutputRatio) {
            val secondAmount = capRecoveredAmount(top[1].second)
            if (secondAmount > 0) {
                ret += top[1].first to secondAmount
            }
        }
        return ret
    }

    private fun capRecoveredAmount(amount: Double): Int {
        return floor(amount).toInt().coerceAtMost(config.maxRecoveredMaterialAmount)
    }

    private fun isTopLevelTarget(loc: ResourceLocation): Boolean {
        if (loc.namespace != "tinactory") {
            return false
        }
        return loc.path.startsWith("component/") ||
            loc.path.startsWith("logistics/") ||
            loc.path.startsWith("machine/") ||
            loc.path.startsWith("multiblock/") ||
            loc.path.startsWith("network/")
    }

    private fun clearResolved() {
        selectedRecipeByItem.clear()
        compositionByItem.clear()
        compositionByRecipe.clear()
        visitingItems.clear()
        visitingRecipes.clear()
    }
}
