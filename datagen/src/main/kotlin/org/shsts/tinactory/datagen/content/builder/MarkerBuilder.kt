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
import org.shsts.tinactory.AllRecipes
import org.shsts.tinactory.api.recipe.IProcessingIngredient
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.MarkerRecipe
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.integration.material.MaterialSet
import org.shsts.tinactory.integration.recipe.ProcessingHelper
import org.shsts.tinactory.integration.recipe.TagIngredient
import org.shsts.tinycorelib.datagen.api.recipe.IRecipeFactory
import java.util.Optional

class MarkerFactory : RecipeFactory<MarkerRecipe, MarkerBuilder>(AllRecipes.MARKER, ::MarkerBuilder) {
    override fun onBuild(loc: ResourceLocation, builder: MarkerBuilder) {
        dataGen {
            trackLang(ProcessingRecipe.getDescriptionId(loc))
        }
    }
}

class MarkerBuilder(parent: IRecipeFactory<MarkerRecipe, MarkerBuilder>) :
    ProcessingRecipeBuilder<MarkerRecipe, MarkerBuilder>(parent) {
    private var baseType: String? = null
    private var baseTypeId: ResourceLocation? = null
    private var prefix = ""
    private var requireMultiblock = false
    private var displayIngredient: IProcessingIngredient? = null
    private var displayTex: ResourceLocation? = null
    private val markerOutputs = mutableListOf<ProcessingRecipe.Input>()

    fun baseType(id: String) {
        baseType = id
        baseTypeId = modLoc(id)
    }

    fun baseType(type: RecipeType<*>) {
        baseTypeId = checkNotNull(BuiltInRegistries.RECIPE_TYPE.getKey(type))
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
        displayIngredient = ProcessingHelper.itemIngredient(ItemStack(value))
        displayTex = null
    }

    fun display(tag: TagKey<Item>) {
        displayIngredient = TagIngredient(tag, 1)
        displayTex = null
    }

    fun display(tex: ResourceLocation) {
        displayTex = tex
        displayIngredient = null
    }

    override fun output(item: ItemLike, amount: Int, port: Int, rate: Double) {
        markerOutputs += ProcessingRecipe.Input(port, ProcessingHelper.itemIngredient(ItemStack(item, amount)))
    }

    override fun output(fluid: Fluid, amount: Int, port: Int, rate: Double) {
        markerOutputs += ProcessingRecipe.Input(port, ProcessingHelper.fluidIngredient(FluidStack(fluid, amount)))
    }

    fun output(fluid: Fluid, port: Int) {
        output(fluid, 1, port)
    }

    fun output(tag: TagKey<Item>, port: Int) {
        markerOutputs += ProcessingRecipe.Input(port, TagIngredient(tag, 1))
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
        return MarkerRecipe(inputs, outputs, voltage!!.value,
            baseTypeId!!, prefix, requireMultiblock,
            Optional.ofNullable(displayIngredient),
            Optional.ofNullable(displayTex),
            markerOutputs)
    }

    override fun validate() {
        checkNotNull(baseTypeId)
    }

    override fun buildObject(): MarkerRecipe {
        if (voltage == null) {
            voltage = Voltage.PRIMITIVE
        }
        return super.buildObject()
    }
}
