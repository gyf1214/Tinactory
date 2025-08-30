package org.shsts.tinactory.datagen.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import org.shsts.tinactory.content.AllRegistries.ITEMS
import org.shsts.tinactory.core.util.LocHelper.mcLoc

object RegistryHelper {
    fun getItem(loc: ResourceLocation) = ITEMS.getEntry<Item>(loc).get()!!

    fun vanillaItem(id: String) = getItem(mcLoc(id))

    fun modItem(id: String) = ITEMS.getEntry<Item>(id).get()
}
