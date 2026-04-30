package org.shsts.tinactory.datagen.content.recovery

import org.shsts.tinactory.integration.material.MaterialSet

class RecoveryComposition(private val amounts: Map<MaterialSet, Double> = mapOf()) {
    fun plus(other: RecoveryComposition): RecoveryComposition {
        val ret = amounts.toMutableMap()
        for ((material, amount) in other.amounts) {
            ret[material] = (ret[material] ?: 0.0) + amount
        }
        return RecoveryComposition(ret.filterValues { it > 0.0 })
    }

    fun scale(factor: Double): RecoveryComposition {
        return RecoveryComposition(amounts.mapValues { it.value * factor }.filterValues { it > 0.0 })
    }

    fun total(): Double {
        return amounts.values.sum()
    }

    fun isEmpty(): Boolean {
        return amounts.isEmpty()
    }

    fun topMaterials(limit: Int): List<Pair<MaterialSet, Double>> {
        return amounts.entries
            .sortedWith(compareByDescending<Map.Entry<MaterialSet, Double>> { it.value }
                .thenBy { it.key.loc(RecoveryRegistry.targetSub).toString() })
            .take(limit)
            .map { it.key to it.value }
    }
}
