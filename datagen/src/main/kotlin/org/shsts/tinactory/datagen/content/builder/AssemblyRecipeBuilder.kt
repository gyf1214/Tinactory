package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.AllTags
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.recovery.RecoveryInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryItemInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryMaterialInput
import org.shsts.tinactory.datagen.content.recovery.RecoveryOutput
import org.shsts.tinactory.datagen.content.recovery.RecoveryRegistry
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory
import kotlin.math.max

abstract class AssemblyRecipeBuilder<R : AssemblyRecipe, B : AssemblyRecipeBuilder<R, B>>(
    parent: IRecipeFactory<R, B>) : ProcessingRecipeBuilder<R, B>(parent) {
    var recordRecovery: Boolean = false
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
        if (recordRecovery) {
            recoveryInputs += RecoveryMaterialInput(mat, sub, amount.toDouble())
        }
    }

    override fun recordItemInput(item: ItemLike, amount: Int) {
        if (recordRecovery) {
            recoveryInputs += RecoveryItemInput(item, amount.toDouble())
        }
    }

    override fun recordItemOutput(item: ItemLike, amount: Int) {
        if (recordRecovery) {
            recoveryOutputs += RecoveryOutput(item, amount, voltage)
        }
    }

    override fun buildObject(): R {
        if (autoCable) {
            component("cable", amount = max(2, components * 2))
        }
        if (recoveryInputs.isNotEmpty() && recoveryOutputs.size == 1) {
            RecoveryRegistry.record(recoveryOutputs.single(), recoveryInputs)
        }
        return super.buildObject()
    }
}

class SimpleAssemblyRecipeBuilder(parent: IRecipeFactory<AssemblyRecipe, SimpleAssemblyRecipeBuilder>) :
    AssemblyRecipeBuilder<AssemblyRecipe, SimpleAssemblyRecipeBuilder>(parent) {
    override fun createObject(): AssemblyRecipe {
        return AssemblyRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!, requiredTech)
    }
}

class OreAnalyzerRecipeBuilder(parent: IRecipeFactory<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>) :
    AssemblyRecipeBuilder<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>(parent) {
    private var rate: Double? = null

    fun rate(value: Double) {
        rate = value
    }

    override fun validate() {
        super.validate()
        check(rate!! > 0)
    }

    override fun createObject(): OreAnalyzerRecipe {
        return OreAnalyzerRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!, requiredTech, rate!!)
    }
}
