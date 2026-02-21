package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.IDataGen
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

object DataFactories {
    fun itemData(block: ItemDataFactory.() -> Unit) {
        ItemDataFactory().apply(block)
    }

    fun <U : Item> itemData(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
        itemData { item(entry, block) }
    }

    fun itemData(id: String, block: IItemDataBuilder<Item, *>.() -> Unit) {
        itemData { item(id, block) }
    }

    fun blockData(block: BlockDataFactory.() -> Unit) {
        BlockDataFactory().apply(block)
    }

    fun <U : Block> blockData(entry: IEntry<U>, block: IBlockDataBuilder<U, *>.() -> Unit) {
        blockData { block(entry, block) }
    }

    fun blockData(id: String, block: IBlockDataBuilder<Block, *>.() -> Unit) {
        blockData { block(id, block) }
    }

    fun dataGen(block: IDataGen.() -> Unit) {
        DATA_GEN.apply(block)
    }
}
