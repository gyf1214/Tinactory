package org.shsts.tinactory.datagen.content.builder

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.MaterialSet
import org.shsts.tinactory.core.recipe.ProcessingIngredients
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ProcessingResults

open class ProcessingRecipeBuilder<B : ProcessingRecipe.BuilderBase<*, B>>(protected val builder: B) {
    protected var voltage: Voltage? = null
    var amperage: Double? = null
    var defaultInputItem: Int? = null
    var defaultInputFluid: Int? = null
    var defaultOutputItem: Int? = null
    var defaultOutputFluid: Int? = null
    var defaultItemSub: String? = null
    var defaultFluidSub = "fluid"
    var requirePower = true

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

    fun input(tag: TagKey<Item>, amount: Int = 1, port: Int = defaultInputItem!!) {
        builder.input(port) { ProcessingIngredients.TagIngredient(tag, amount) }
    }

    fun input(item: ItemLike, amount: Int = 1, port: Int = defaultInputItem!!) {
        builder.input(port) { ProcessingIngredients.ItemIngredient(ItemStack(item, amount)) }
    }

    fun input(fluid: Fluid, amount: Int, port: Int = defaultInputFluid!!) {
        builder.input(port) { ProcessingIngredients.FluidIngredient(FluidStack(fluid, amount)) }
    }

    fun input(mat: MaterialSet, sub: String = defaultSub(mat), amount: Number = 1, port: Int? = null) {
        if (mat.hasFluid(sub)) {
            input(mat.fluid(sub).get(), mat.fluidAmount(amount.toFloat()),
                port ?: defaultInputFluid!!)
        } else {
            input(mat.tag(sub), amount.toInt(), port ?: defaultInputItem!!)
        }
    }

    fun input(name: String, sub: String = defaultSub(name), amount: Number = 1, port: Int? = null) {
        input(getMaterial(name), sub, amount, port)
    }

    fun output(item: ItemLike, amount: Int = 1, port: Int = defaultOutputItem!!, rate: Double = 1.0) {
        builder.output(port) {
            ProcessingResults.ItemResult(rate, ItemStack(item, amount))
        }
    }

    fun output(fluid: Fluid, amount: Int, port: Int = defaultOutputFluid!!, rate: Double = 1.0) {
        builder.output(port) {
            ProcessingResults.FluidResult(rate, FluidStack(fluid, amount))
        }
    }

    fun output(mat: MaterialSet, sub: String = defaultSub(mat),
        amount: Number = 1, port: Int? = null, rate: Double = 1.0) {
        if (mat.hasFluid(sub)) {
            output(mat.fluid(sub).get(), mat.fluidAmount(amount.toFloat()),
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
        builder.block()
    }

    fun workTicks(value: Long) {
        builder.workTicks(value)
    }

    fun voltage(value: Voltage) {
        voltage = value
        builder.voltage(value.value)
    }

    private fun power(voltage: Voltage?, amperage: Double?): Long? {
        if (voltage != null && amperage != null) {
            val voltage1 = if (voltage == Voltage.PRIMITIVE) Voltage.ULV.value else voltage.value
            return (amperage * voltage1).toLong()
        } else {
            return null
        }
    }

    open fun build() {
        val power = power(voltage, amperage)
        if (requirePower || power != null) {
            builder.power(power!!)
        }
        builder.build()
    }
}
