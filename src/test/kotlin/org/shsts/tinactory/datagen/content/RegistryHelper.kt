package org.shsts.tinactory.datagen.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.AllRegistries.BLOCKS
import org.shsts.tinactory.AllRegistries.ITEMS
import org.shsts.tinactory.core.util.LocHelper.mcLoc

object RegistryHelper {
    fun blockEntry(id: String) = BLOCKS.getEntry<Block>(id)

    fun getBlock(id: String) = blockEntry(id).get()

    fun itemEntry(id: String) = ITEMS.getEntry<Item>(id)

    fun getItem(loc: ResourceLocation) = ITEMS.getEntry<Item>(loc).get()!!

    fun getItem(id: String) = ITEMS.getEntry<Item>(id).get()!!

    fun vanillaItem(id: String) = getItem(mcLoc(id))
}
