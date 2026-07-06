package org.shsts.tinactory.datagen.content

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.AllRegistries.BLOCKS
import org.shsts.tinactory.AllRegistries.ITEMS
import org.shsts.tinactory.core.util.LocHelper.mcLoc

object RegistryHelper {
    fun blockEntry(id: String) = BLOCKS.getEntry<Block>(id)

    fun itemEntry(id: String) = ITEMS.getEntry<Item>(id)

    fun getItem(id: String) = itemEntry(id).get()!!

    fun vanillaItem(id: String) = ITEMS.getEntry<Item>(mcLoc(id)).get()!!

    fun itemLoc(item: ItemLike) = BuiltInRegistries.ITEM.getKey(item.asItem())

    fun itemKey(item: ItemLike) = BuiltInRegistries.ITEM.getResourceKey(item.asItem()).orElseThrow()
}
