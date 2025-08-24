package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

object DataFactories {
    fun <U : Item> item(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
        DATA_GEN.item(entry).apply {
            block()
            build()
        }
    }

    fun <U : Block> block(entry: IEntry<U>, block: IBlockDataBuilder<U, *>.() -> Unit) {
        DATA_GEN.block(entry).apply {
            block()
            build()
        }
    }
}
