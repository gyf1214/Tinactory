package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.recovery.RecoveryInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryItemInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryMaterialInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryOutput
import org.shsts.tinactory.datagen.content.recovery.RecoveryRecipe
import org.shsts.tinactory.datagen.content.recovery.RecoveryRecipeKey
import org.shsts.tinactory.datagen.content.recovery.RecoveryRegistry
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory
import kotlin.math.max

open class AssemblyRecipeBuilder<R : AssemblyRecipe, B : AssemblyRecipeBuilder<R, B>>(
    parent: IRecipeFactory<R, B>,
    private val assemblyFactory: (
        List<ProcessingRecipe.Input>, List<ProcessingRecipe.Output>, Long, Long, Long,
        List<ResourceLocation>) -> R
) : ProcessingRecipeBuilder<R, B>(parent, { inputs, outputs, workTicks, voltage, power ->
    assemblyFactory(inputs, outputs, workTicks, voltage, power, listOf())
}) {
    var componentVoltage: Voltage? = null
    var autoCable = false
    private var components = 0
    protected val requiredTech = mutableListOf<ResourceLocation>()
    private val recoveryInputs = mutableListOf<RecoveryInput>()
    private val recoveryOutputs = mutableListOf<RecoveryOutput>()

    fun tech(vararg loc: ResourceLocation) {
        requiredTech += loc
    }

    fun requireTech(vararg loc: ResourceLocation) {
        tech(*loc)
    }

    fun component(name: String, amount: Int = 1,
        voltage: Voltage = this.componentVoltage!!, port: Int = defaultInputItem!!) {
        val component = getComponent(name)
        input(component.item(voltage), amount, port)
        if (autoCable) {
            components += amount
            if (name == "cable") {
                autoCable = false
            }
        }
    }

    fun machine(name: String, amount: Int = 1,
        voltage: Voltage = this.componentVoltage!!, port: Int = defaultInputItem!!) {
        input(getMachine(name).block(voltage), amount, port)
    }

    fun circuit(amount: Int, voltage: Voltage = this.componentVoltage!!,
        port: Int = defaultInputItem!!) {
        input(AllTags.circuit(voltage), amount, port)
    }

    fun pic(amount: Int) {
        val v = componentVoltage!!
        if (v.rank < Voltage.HV.rank) {
            return
        } else if (v.rank < Voltage.IV.rank) {
            input(CHIP.item("low_pic"), amount)
        } else if (v.rank < Voltage.ZPM.rank) {
            input(CHIP.item("pic"), amount)
        } else {
            input(CHIP.item("high_pic"), amount)
        }
    }

    override fun recordMaterialInput(mat: MaterialSet, sub: String, amount: Number) {
        recoveryInputs += RecoveryMaterialInput(mat, sub, amount.toDouble())
    }

    override fun recordItemInput(item: ItemLike, amount: Int) {
        recoveryInputs += RecoveryItemInput(item, amount.toDouble())
    }

    override fun recordItemOutput(item: ItemLike, amount: Int) {
        recoveryOutputs += RecoveryOutput(item, amount, voltage)
    }

    override fun createObject(): R {
        return assemblyFactory(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            requiredTech.toList())
    }

    override fun build(): IRecipeFactory<R, B> {
        if (autoCable) {
            component("cable", amount = max(2, components * 2))
        }
        if (recoveryInputs.isNotEmpty() && recoveryOutputs.size == 1) {
            RecoveryRegistry.record(RecoveryRecipe(
                RecoveryRecipeKey(ResourceLocation("tinactory", "deferred_recipe_id")),
                recoveryOutputs.single(),
                recoveryInputs.toList()))
        }
        return super.build()
    }
}

class SimpleAssemblyRecipeBuilder(parent: IRecipeFactory<AssemblyRecipe, SimpleAssemblyRecipeBuilder>) :
    AssemblyRecipeBuilder<AssemblyRecipe, SimpleAssemblyRecipeBuilder>(parent, ::AssemblyRecipe)
