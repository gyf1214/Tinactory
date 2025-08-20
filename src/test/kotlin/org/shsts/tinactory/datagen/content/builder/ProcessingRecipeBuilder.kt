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

class ProcessingRecipeBuilder<B : ProcessingRecipe.BuilderBase<*, B>>(val builder: B) {
    private var voltage: Long? = null
    var amperage: Double? = null
    var defaultInputItem: Int? = null
    var defaultInputFluid: Int? = null
    var defaultOutputItem: Int? = null
    var defaultOutputFluid: Int? = null

    fun defaults(inputItem: Int, inputFluid: Int, outputItem: Int, outputFluid: Int) {
        defaultInputItem = inputItem
        defaultInputFluid = inputFluid
        defaultOutputItem = outputItem
        defaultOutputFluid = outputFluid
    }

    fun fullDefaults() {
        defaults(0, 1, 2, 3)
    }

    fun simpleDefaults() {
        defaultInputItem = 0
        defaultOutputItem = 1
    }

    fun inputTag(tag: TagKey<Item>, amount: Int = 1, port: Int = defaultInputItem!!) {
        builder.input(port) { ProcessingIngredients.TagIngredient(tag, amount) }
    }

    fun inputFluid(fluid: () -> Fluid, amount: Int, port: Int = defaultInputFluid!!) {
        builder.input(port) {
            ProcessingIngredients.FluidIngredient(FluidStack(fluid(), amount))
        }
    }

    fun inputMaterial(mat: MaterialSet, sub: String, amount: Number = 1, port: Int? = null) {
        if (mat.hasFluid(sub)) {
            inputFluid(mat.fluid(sub)::get, mat.fluidAmount(amount.toFloat()),
                port ?: defaultInputFluid!!)
        } else {
            inputTag(mat.tag(sub), amount.toInt(), port ?: defaultInputItem!!)
        }
    }

    fun inputMaterial(name: String, sub: String, amount: Number = 1, port: Int? = null) {
        inputMaterial(getMaterial(name), sub, amount, port)
    }

    fun outputItem(item: () -> ItemLike, amount: Int = 1,
        port: Int = defaultOutputItem!!, rate: Double = 1.0) {
        builder.output(port) {
            ProcessingResults.ItemResult(rate, ItemStack(item(), amount))
        }
    }

    fun outputItem(item: ItemLike, amount: Int = 1,
        port: Int = defaultOutputItem!!, rate: Double = 1.0) {
        outputItem({ item }, amount, port, rate)
    }

    fun outputFluid(fluid: () -> Fluid, amount: Int,
        port: Int = defaultOutputFluid!!, rate: Double = 1.0) {
        builder.output(port) {
            ProcessingResults.FluidResult(rate, FluidStack(fluid(), amount))
        }
    }

    fun outputMaterial(mat: MaterialSet, sub: String, amount: Number = 1, port: Int? = null,
        rate: Double = 1.0) {
        if (mat.hasFluid(sub)) {
            outputFluid(mat.fluid(sub)::get, mat.fluidAmount(amount.toFloat()),
                port ?: defaultOutputFluid!!, rate)
        } else {
            outputItem(mat.entry(sub)::get, amount.toInt(), port ?: defaultOutputItem!!, rate)
        }
    }

    fun outputMaterial(name: String, sub: String, amount: Number = 1, port: Int? = null,
        rate: Double = 1.0) {
        outputMaterial(getMaterial(name), sub, amount, port, rate)
    }

    fun extra(block: B.() -> Unit) {
        builder.block()
    }

    fun workTicks(value: Long) {
        builder.workTicks(value)
    }

    fun voltage(value: Voltage) {
        voltage = value.value
    }

    fun build() {
        val voltage1 = voltage!!
        val voltage2 = if (voltage1 == 0L) Voltage.ULV.value else voltage1
        val power = (amperage!! * voltage2).toLong()
        builder.voltage(voltage1).power(power).build()
    }
}
