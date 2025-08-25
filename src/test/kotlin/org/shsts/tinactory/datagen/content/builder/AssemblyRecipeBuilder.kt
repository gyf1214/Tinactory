package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.Component
import org.shsts.tinactory.datagen.content.component.item

class AssemblyRecipeBuilder(builder: AssemblyRecipe.Builder) :
    ProcessingRecipeBuilder<AssemblyRecipe.Builder>(builder) {
    var componentVoltage: Voltage? = null

    fun tech(vararg loc: ResourceLocation) {
        builder.requireTech(*loc)
    }

    fun input(component: Component, amount: Int = 1,
        voltage: Voltage = this.componentVoltage!!, port: Int = defaultInputItem!!) {
        input(component.item(voltage), amount, port)
    }

    fun circuit(amount: Int, voltage: Voltage = this.componentVoltage!!,
        port: Int = defaultInputItem!!) {
        input(AllTags.circuit(voltage), amount, port)
    }
}
