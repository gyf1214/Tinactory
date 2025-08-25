package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.Component
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

class AssemblyRecipeFactory(
    recipeType: IRecipeType<AssemblyRecipe.Builder>,
    defaults: AssemblyRecipeBuilder.() -> Unit) :
    RecipeFactory<AssemblyRecipe.Builder, AssemblyRecipeBuilder>(
        recipeType, ::AssemblyRecipeBuilder, defaults) {
    var componentVoltage: Voltage? = null

    fun output(component: Component, amount: Int = 1,
        suffix: String = "", voltage: Voltage = this.componentVoltage!!,
        rate: Double = 1.0, block: AssemblyRecipeBuilder.() -> Unit = {}) {
        if (!component.containsKey(voltage)) {
            return
        }
        output(component.item(voltage), amount, suffix, rate) {
            componentVoltage = voltage
            block()
        }
    }
}
