package org.shsts.tinactory.datagen.content.recovery

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.integration.material.MaterialSet

data class RecoveryRecipeKey(val loc: ResourceLocation)

data class RecoveryOutput(
    val item: ItemLike,
    val amount: Int,
    val voltage: Voltage?)

sealed interface RecoveryInput {
    val amount: Double
}

data class RecoveryMaterialInput(
    val material: MaterialSet,
    val sub: String,
    override val amount: Double) : RecoveryInput

data class RecoveryItemInput(
    val item: ItemLike,
    override val amount: Double) : RecoveryInput

data class RecoveryRecipe(
    val key: RecoveryRecipeKey,
    val output: RecoveryOutput,
    val inputs: List<RecoveryInput>)
