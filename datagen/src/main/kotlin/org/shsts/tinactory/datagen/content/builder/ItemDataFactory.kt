package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.item.Item
import org.shsts.tinactory.datagen.content.RegistryHelper.itemEntry
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

class ItemDataFactory : DataFactory<IItemDataBuilder<*, *>>() {
    fun <U : Item> item(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
        build(DATA_GEN.item(entry), block)
    }

    fun item(id: String, block: IItemDataBuilder<Item, *>.() -> Unit) {
        build(DATA_GEN.item(itemEntry(id)), block)
    }
}
