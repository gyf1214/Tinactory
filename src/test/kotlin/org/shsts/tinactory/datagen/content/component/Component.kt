package org.shsts.tinactory.datagen.content.component

import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.content.electric.Voltage
import java.util.function.Supplier

typealias Component = Map<Voltage, Supplier<out ItemLike>>
typealias ListComponent = List<Supplier<out ItemLike>>

fun Component.item(v: Voltage) = getValue(v).get()

fun ListComponent.item(i: Int) = get(i).get()
