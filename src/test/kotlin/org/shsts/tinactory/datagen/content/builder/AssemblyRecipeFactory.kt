package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

class AssemblyRecipeFactory(
    recipeType: IRecipeType<AssemblyRecipe.Builder>,
    defaults: AssemblyRecipeBuilder.() -> Unit) :
    RecipeFactory<AssemblyRecipe.Builder, AssemblyRecipeBuilder>(
        recipeType, ::AssemblyRecipeBuilder, defaults) {
    var componentVoltage: Voltage? = null

    override fun classDefaults(builder: AssemblyRecipeBuilder) {
        super.classDefaults(builder)
        builder.componentVoltage = componentVoltage
    }

    fun component(name: String, voltage: Voltage = this.componentVoltage!!,
        block: AssemblyRecipeBuilder.() -> Unit = {}) {
        val component = getComponent(name)
        if (!component.containsKey(voltage)) {
            return
        }
        output(component.item(voltage)) {
            componentVoltage = voltage
            block()
        }
    }

    fun machine(name: String, voltage: Voltage = this.componentVoltage!!,
        block: AssemblyRecipeBuilder.() -> Unit = {}) {
        val machine = getMachine(name)
        if (!machine.hasVoltage(voltage)) {
            return
        }
        output(machine.block(voltage)) {
            componentVoltage = voltage
            block()
        }
    }
}
