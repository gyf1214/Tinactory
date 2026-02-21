package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.fluids.FluidStack
import org.shsts.tinactory.core.material.MaterialSet
import org.shsts.tinactory.core.recipe.MarkerRecipe
import org.shsts.tinactory.core.recipe.ProcessingIngredients
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen

class MarkerBuilder(builder: MarkerRecipe.Builder) :
    ProcessingRecipeBuilder<MarkerRecipe.Builder>(builder) {
    private var baseType: String? = null

    init {
        requirePower = false
        val loc = builder.loc
        builder.onBuild {
            dataGen {
                trackLang(ProcessingRecipe.getDescriptionId(loc))
            }
        }
    }

    fun baseType(id: String) {
        baseType = id
        builder.baseType(modLoc(id))
    }

    fun baseType(type: RecipeType<*>) {
        builder.baseType(ResourceLocation(type.toString()))
    }

    fun prefix(value: String) {
        if (baseType != null) {
            builder.prefix("$baseType/$value")
        } else {
            builder.prefix(value)
        }
    }

    fun prefix() {
        builder.prefix(baseType!!).requireMultiblock(true)
    }

    override fun output(item: ItemLike, amount: Int, port: Int, rate: Double) {
        builder.output(port, ProcessingIngredients.ItemIngredient(ItemStack(item, amount)))
    }

    override fun output(fluid: Fluid, amount: Int, port: Int, rate: Double) {
        builder.output(port, ProcessingIngredients.FluidIngredient(FluidStack(fluid, amount)))
    }

    fun output(fluid: Fluid, port: Int) {
        output(fluid, 1, port)
    }

    fun output(tag: TagKey<Item>, port: Int) {
        builder.output(port, ProcessingIngredients.TagIngredient(tag, 1))
    }

    override fun output(mat: MaterialSet, sub: String, amount: Number, port: Int?, rate: Double) {
        if (mat.hasFluid(sub)) {
            output(mat.fluid(sub).get(), mat.fluidAmount(sub, amount.toFloat()),
                port ?: defaultOutputFluid!!, rate)
        } else {
            output(mat.tag(sub), port ?: defaultOutputItem!!)
        }
    }
}
