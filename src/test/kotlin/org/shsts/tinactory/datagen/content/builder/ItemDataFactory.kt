package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.item.Item
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

class ItemDataFactory : DataFactory<IItemDataBuilder<*, *>>() {
    fun <U : Item> item(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
        build(DATA_GEN.item(entry), block)
    }
}
