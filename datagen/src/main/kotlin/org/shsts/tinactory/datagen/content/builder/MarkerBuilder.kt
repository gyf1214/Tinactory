package org.shsts.tinactory.datagen.content.builder

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.fluids.FluidStack
import org.shsts.tinactory.api.recipe.IProcessingIngredient
import org.shsts.tinactory.core.recipe.MarkerRecipe
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinactory.integration.recipe.ProcessingHelper
import org.shsts.tinactory.integration.recipe.TagIngredient
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory
import java.util.Optional

class MarkerBuilder(parent: IRecipeFactory<MarkerRecipe, MarkerBuilder>) :
    ProcessingRecipeBuilder<MarkerRecipe, MarkerBuilder>(
        parent, { inputs, outputs, workTicks, voltage, power ->
            MarkerRecipe(inputs, outputs, workTicks, voltage, power, modLoc("processing"),
                "", false, Optional.empty(), Optional.empty(), listOf())
        }) {
    private var baseType: String? = null
    private var baseTypeId = modLoc("processing")
    private var prefix = ""
    private var requireMultiblock = false
    private var displayIngredient: Optional<IProcessingIngredient> = Optional.empty()
    private var displayTex: Optional<ResourceLocation> = Optional.empty()
    private val markerOutputs = mutableListOf<MarkerRecipe.Input>()

    init { requirePower = false }

    fun baseType(id: String) {
        baseType = id
        baseTypeId = modLoc(id)
    }

    fun baseType(type: RecipeType<*>) {
        baseTypeId = checkNotNull(BuiltInRegistries.RECIPE_TYPE.getKey(type)) { type.toString() }
    }

    fun prefix(value: String) {
        prefix = if (baseType != null) {
            "$baseType/$value"
        } else {
            value
        }
    }

    fun prefix() {
        prefix = baseType!!
        requireMultiblock = true
    }

    fun requireMultiblock(value: Boolean) {
        requireMultiblock = value
    }

    fun display(value: ItemLike) {
        displayIngredient = Optional.of(ProcessingHelper.itemIngredient(ItemStack(value)))
        displayTex = Optional.empty()
    }

    fun display(tag: TagKey<Item>) {
        displayIngredient = Optional.of(TagIngredient(tag, 1))
        displayTex = Optional.empty()
    }

    fun display(tex: ResourceLocation) {
        displayTex = Optional.of(tex)
        displayIngredient = Optional.empty()
    }

    override fun output(item: ItemLike, amount: Int, port: Int, rate: Double) {
        markerOutputs += MarkerRecipe.Input(port, ProcessingHelper.itemIngredient(ItemStack(item, amount)))
    }

    override fun output(fluid: Fluid, amount: Int, port: Int, rate: Double) {
        markerOutputs += MarkerRecipe.Input(port, ProcessingHelper.fluidIngredient(FluidStack(fluid, amount)))
    }

    fun output(fluid: Fluid, port: Int) {
        output(fluid, 1, port)
    }

    fun output(tag: TagKey<Item>, port: Int) {
        markerOutputs += MarkerRecipe.Input(port, TagIngredient(tag, 1))
    }

    override fun output(mat: MaterialSet, sub: String, amount: Number, port: Int?, rate: Double) {
        if (mat.hasFluid(sub)) {
            output(mat.fluid(sub).get(), mat.fluidAmount(sub, amount.toFloat()),
                port ?: defaultOutputFluid!!, rate)
        } else {
            output(mat.tag(sub), port ?: defaultOutputItem!!)
        }
    }

    override fun createObject(): MarkerRecipe {
        return MarkerRecipe(inputs.toList(), outputs.toList(), workTicks, voltageValue, power,
            baseTypeId, prefix, requireMultiblock, displayIngredient, displayTex,
            markerOutputs.toList())
    }
}
