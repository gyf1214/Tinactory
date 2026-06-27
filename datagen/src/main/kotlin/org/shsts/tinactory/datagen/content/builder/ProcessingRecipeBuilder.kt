package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
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
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ResearchRecipe
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinactory.integration.recipe.ProcessingHelper
import org.shsts.tinactory.integration.recipe.TagIngredient
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory

open class ProcessingRecipeBuilder<R : ProcessingRecipe, B : ProcessingRecipeBuilder<R, B>>(
    parent: IRecipeFactory<R, B>,
    private val factory: (List<ProcessingRecipe.Input>, List<ProcessingRecipe.Output>, Long, Long, Long) -> R
) : Builder<R, IRecipeFactory<R, B>, B>(parent) {
    var voltage: Voltage? = null
    var amperage: Double? = null
    var defaultInputItem: Int? = null
    var defaultInputFluid: Int? = null
    var defaultOutputItem: Int? = null
    var defaultOutputFluid: Int? = null
    var defaultItemSub: String? = null
    var defaultFluidSub = "fluid"
    var requirePower = true
    val inputs = mutableListOf<ProcessingRecipe.Input>()
    val outputs = mutableListOf<ProcessingRecipe.Output>()
    protected var workTicks = 0L
    protected var voltageValue = 0L
    protected var power = 0L

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
        voltageValue = value.value
    }

    private fun power(voltage: Voltage?, amperage: Double?): Long? {
        if (voltage != null && amperage != null) {
            val voltage1 = if (voltage == Voltage.PRIMITIVE) Voltage.ULV.value else voltage.value
            return (amperage * voltage1).toLong()
        } else {
            return null
        }
    }

    override fun createObject(): R {
        return factory(inputs.toList(), outputs.toList(), workTicks, voltageValue, power)
    }

    override fun build(): IRecipeFactory<R, B> {
        val calculatedPower = power(voltage, amperage)
        if (requirePower || calculatedPower != null) {
            power = calculatedPower!!
        }
        return super.build()
    }
}

class SimpleProcessingBuilder(parent: IRecipeFactory<ProcessingRecipe, SimpleProcessingBuilder>) :
    ProcessingRecipeBuilder<ProcessingRecipe, SimpleProcessingBuilder>(parent, ::ProcessingRecipe)

class CleanRecipeBuilder(parent: IRecipeFactory<CleanRecipe, CleanRecipeBuilder>) :
    ProcessingRecipeBuilder<CleanRecipe, CleanRecipeBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power ->
            CleanRecipe(inputs, outputs, workTicks, voltage, power, 0.0, 0.0)
        }) {
    private var minCleanness = 0.0
    private var maxCleanness = 0.0

    fun requireCleanness(min: Double, max: Double) {
        minCleanness = min
        maxCleanness = max
    }

    override fun createObject(): CleanRecipe {
        return CleanRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            minCleanness, maxCleanness)
    }
}

class GeneratorRecipeBuilder(parent: IRecipeFactory<GeneratorRecipe, GeneratorRecipeBuilder>) :
    ProcessingRecipeBuilder<GeneratorRecipe, GeneratorRecipeBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power ->
            GeneratorRecipe(inputs, outputs, workTicks, voltage, power, false)
        }) {
    private var exactVoltage = false

    fun exactVoltage(value: Boolean) {
        exactVoltage = value
    }

    override fun createObject(): GeneratorRecipe {
        return GeneratorRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            exactVoltage)
    }
}

class BlastFurnaceBuilder(parent: IRecipeFactory<BlastFurnaceRecipe, BlastFurnaceBuilder>) :
    ProcessingRecipeBuilder<BlastFurnaceRecipe, BlastFurnaceBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power ->
            BlastFurnaceRecipe(inputs, outputs, workTicks, voltage, power, 0)
        }) {
    private var temperature = 0

    fun temperature(value: Int) {
        temperature = value
    }

    override fun createObject(): BlastFurnaceRecipe {
        return BlastFurnaceRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            temperature)
    }
}

class OreAnalyzerRecipeBuilder(parent: IRecipeFactory<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>) :
    AssemblyRecipeBuilder<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power, requiredTech ->
            OreAnalyzerRecipe(inputs, outputs, workTicks, voltage, power, requiredTech, 0.0)
        }) {
    private var rate = 0.0

    fun rate(value: Double) {
        rate = value
    }

    override fun createObject(): OreAnalyzerRecipe {
        return OreAnalyzerRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            requiredTech.toList(), rate)
    }
}

class ResearchRecipeBuilder(parent: IRecipeFactory<ResearchRecipe, ResearchRecipeBuilder>) :
    ProcessingRecipeBuilder<ResearchRecipe, ResearchRecipeBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power ->
            ResearchRecipe(inputs, outputs, workTicks, voltage, power, ResourceLocation("tinactory", "missing"), 1)
        }) {
    private var target: ResourceLocation? = null
    private var progress = 1L

    fun target(value: ResourceLocation) {
        target = value
    }

    fun progress(value: Long) {
        progress = value
    }

    override fun createObject(): ResearchRecipe {
        return ResearchRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            checkNotNull(target) { "Missing research target" }, progress)
    }
}
