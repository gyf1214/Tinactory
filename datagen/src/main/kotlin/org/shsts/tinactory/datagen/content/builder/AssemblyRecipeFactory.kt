package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

class AssemblyRecipeFactory(
    recipeType: IRecipeType<AssemblyRecipe>,
    defaults: SimpleAssemblyRecipeBuilder.() -> Unit) :
    RecipeFactory<AssemblyRecipe, SimpleAssemblyRecipeBuilder>(
        recipeType, ::SimpleAssemblyRecipeBuilder, defaults) {
    var componentVoltage: Voltage? = null

    override fun classDefaults(builder: SimpleAssemblyRecipeBuilder) {
        super.classDefaults(builder)
        builder.componentVoltage = componentVoltage
        builder.recordRecovery = true
    }

    override fun onBuild(loc: ResourceLocation, builder: SimpleAssemblyRecipeBuilder) {
        builder.onBuild { builder.recordRecovery(loc) }
    }

    fun component(name: String, voltage: Voltage = this.componentVoltage!!,
        block: SimpleAssemblyRecipeBuilder.() -> Unit = {}) {
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
        block: SimpleAssemblyRecipeBuilder.() -> Unit = {}) {
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
