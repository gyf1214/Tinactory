package org.shsts.tinactory.datagen.content.builder

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.FluidStack
import org.shsts.tinactory.AllMaterials.getMaterial
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.content.recipe.CleanRecipe
import org.shsts.tinactory.content.recipe.GeneratorRecipe
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinactory.integration.recipe.ProcessingHelper
import org.shsts.tinactory.integration.recipe.TagIngredient
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

abstract class ProcessingRecipeBuilder<R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>>(
    parent: IRecipeFactory<R, B>
) : Builder<R, IRecipeFactory<R, B>, B>(parent) {
    protected val inputs = mutableListOf<ProcessingRecipe.Input>()
    protected val outputs = mutableListOf<ProcessingRecipe.Output>()
    var defaultInputItem: Int? = null
    var defaultInputFluid: Int? = null
    var defaultOutputItem: Int? = null
    var defaultOutputFluid: Int? = null
    var defaultItemSub: String? = null
    var defaultFluidSub = "fluid"
    protected var workTicks: Long? = null
    protected var voltage: Voltage? = null
    var amperage: Double? = null
    protected var power: Long? = null

    fun fullDefaults() {
        defaultInputItem = 0
        defaultInputFluid = 1
        defaultOutputItem = 2
        defaultOutputFluid = 3
    }

    fun simpleDefaults() {
        defaultInputItem = 0
        defaultOutputItem = 1
    }

    private fun defaultSub(mat: MaterialSet) =
        defaultItemSub?.takeIf { mat.hasItem(it) } ?: defaultFluidSub

    private fun defaultSub(name: String) = defaultSub(getMaterial(name))

    protected open fun recordMaterialInput(mat: MaterialSet, sub: String, amount: Number) {}

    protected open fun recordItemInput(item: ItemLike, amount: Int) {}

    protected open fun recordItemOutput(item: ItemLike, amount: Int) {}

    fun input(tag: TagKey<Item>, amount: Int = 1, port: Int = defaultInputItem!!) {
        inputs += ProcessingRecipe.Input(port, TagIngredient(tag, amount))
    }

    fun input(item: ItemLike, amount: Int = 1, port: Int = defaultInputItem!!) {
        recordItemInput(item, amount)
        inputs += ProcessingRecipe.Input(port, ProcessingHelper.itemIngredient(ItemStack(item, amount)))
    }

    fun input(fluid: Fluid, amount: Int, port: Int = defaultInputFluid!!) {
        inputs += ProcessingRecipe.Input(port, ProcessingHelper.fluidIngredient(FluidStack(fluid, amount)))
    }

    fun input(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1, port: Int? = null) {
        if (mat.hasFluid(sub)) {
            input(mat.fluid(sub).get(), mat.fluidAmount(sub, amount.toFloat()),
                port ?: defaultInputFluid!!)
        } else {
            recordMaterialInput(mat, sub, amount)
            input(mat.tag(sub), amount.toInt(), port ?: defaultInputItem!!)
        }
    }

    fun input(name: String, sub: String = defaultSub(name), amount: Number = 1, port: Int? = null) {
        input(getMaterial(name), sub, amount, port)
    }

    open fun output(item: ItemLike, amount: Int = 1, port: Int = defaultOutputItem!!, rate: Double = 1.0) {
        recordItemOutput(item, amount)
        outputs += ProcessingRecipe.Output(port, ProcessingHelper.itemResult(rate, ItemStack(item, amount)))
    }

    open fun output(fluid: Fluid, amount: Int, port: Int = defaultOutputFluid!!, rate: Double = 1.0) {
        outputs += ProcessingRecipe.Output(port, ProcessingHelper.fluidResult(rate, FluidStack(fluid, amount)))
    }

    open fun output(mat: MaterialSet, sub: String = defaultSub(mat),
        amount: Number = 1, port: Int? = null, rate: Double = 1.0) {
        if (mat.hasFluid(sub)) {
            output(mat.fluid(sub).get(), mat.fluidAmount(sub, amount.toFloat()),
                port ?: defaultOutputFluid!!, rate)
        } else {
            output(mat.item(sub), amount.toInt(), port ?: defaultOutputItem!!, rate)
        }
    }

    fun output(name: String, sub: String = defaultSub(name),
        amount: Number = 1, port: Int? = null, rate: Double = 1.0) {
        output(getMaterial(name), sub, amount, port, rate)
    }

    fun extra(block: B.() -> Unit) {
        self().block()
    }

    fun workTicks(value: Long) {
        workTicks = value
    }

    fun voltage(value: Voltage) {
        voltage = value
    }

    private fun power(voltage: Voltage?, amperage: Double?): Long? {
        if (voltage != null && amperage != null) {
            val voltage1 = if (voltage == Voltage.PRIMITIVE) Voltage.ULV.value else voltage.value
            return (amperage * voltage1).toLong()
        } else {
            return null
        }
    }

    protected open fun validate() {
        check(workTicks!! > 0)
        checkNotNull(voltage)
        check(voltage == Voltage.PRIMITIVE || power!! > 0)
    }

    override fun buildObject(): R {
        if (power == null) {
            power = power(voltage, amperage)
        }
        validate()
        return super.buildObject()
    }
}

class SimpleProcessingBuilder(parent: IRecipeFactory<ProcessingRecipe, SimpleProcessingBuilder>) :
    ProcessingRecipeBuilder<ProcessingRecipe, SimpleProcessingBuilder>(parent) {
    override fun createObject(): ProcessingRecipe {
        return ProcessingRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!)
    }
}

class CleanRecipeBuilder(parent: IRecipeFactory<CleanRecipe, CleanRecipeBuilder>) :
    ProcessingRecipeBuilder<CleanRecipe, CleanRecipeBuilder>(parent) {
    private var minCleanness: Double? = null
    private var maxCleanness: Double? = null

    fun requireCleanness(min: Double, max: Double) {
        minCleanness = min
        maxCleanness = max
    }

    override fun validate() {
        super.validate()
        check(minCleanness!! <= maxCleanness!!)
    }

    override fun createObject(): CleanRecipe {
        return CleanRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!,
            minCleanness!!, maxCleanness!!)
    }
}

class GeneratorRecipeBuilder(parent: IRecipeFactory<GeneratorRecipe, GeneratorRecipeBuilder>) :
    ProcessingRecipeBuilder<GeneratorRecipe, GeneratorRecipeBuilder>(parent) {
    private var exactVoltage = false

    fun exactVoltage(value: Boolean) {
        exactVoltage = value
    }

    override fun createObject(): GeneratorRecipe {
        return GeneratorRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!, exactVoltage)
    }
}

class BlastFurnaceBuilder(parent: IRecipeFactory<BlastFurnaceRecipe, BlastFurnaceBuilder>) :
    ProcessingRecipeBuilder<BlastFurnaceRecipe, BlastFurnaceBuilder>(parent) {
    private var temperature: Int? = null

    fun temperature(value: Int) {
        temperature = value
    }

    override fun validate() {
        super.validate()
        checkNotNull(temperature)
    }

    override fun createObject(): BlastFurnaceRecipe {
        return BlastFurnaceRecipe(inputs, outputs, workTicks!!, voltage!!.value, power!!, temperature!!)
    }
}
