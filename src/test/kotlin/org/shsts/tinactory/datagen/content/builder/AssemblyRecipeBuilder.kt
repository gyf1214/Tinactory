package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.datagen.content.component.item
import kotlin.math.max

class AssemblyRecipeBuilder(builder: AssemblyRecipe.Builder) :
    ProcessingRecipeBuilder<AssemblyRecipe.Builder>(builder) {
    var componentVoltage: Voltage? = null
    private var components = 0
    var autoCable = false

    fun tech(vararg loc: ResourceLocation) {
        builder.requireTech(*loc)
    }

    fun component(name: String, amount: Int = 1,
        voltage: Voltage = this.componentVoltage!!, port: Int = defaultInputItem!!) {
        val component = getComponent(name)
        input(component.item(voltage), amount, port)
        if (autoCable) {
            components += amount
            if (name == "cable") {
                autoCable = false
            }
        }
    }

    fun machine(name: String, amount: Int = 1,
        voltage: Voltage = this.componentVoltage!!, port: Int = defaultInputItem!!) {
        input(getMachine(name).block(voltage), amount, port)
    }

    fun circuit(amount: Int, voltage: Voltage = this.componentVoltage!!,
        port: Int = defaultInputItem!!) {
        input(AllTags.circuit(voltage), amount, port)
    }

    fun pic(amount: Int) {
        val v = componentVoltage!!
        if (v.rank < Voltage.HV.rank) {
            return
        } else if (v.rank < Voltage.IV.rank) {
            input(CHIP.item("low_pic"), amount)
        } else if (v.rank < Voltage.ZPM.rank) {
            input(CHIP.item("pic"), amount)
        } else {
            input(CHIP.item("high_pic"), amount)
        }
    }

    override fun build() {
        if (autoCable) {
            component("cable", amount = max(2, components * 2))
        }
        super.build()
    }
}
