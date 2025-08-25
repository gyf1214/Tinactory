package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.IDataGen
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder
import org.shsts.tinycorelib.datagen.api.builder.IDataBuilder
import org.shsts.tinycorelib.datagen.api.builder.IItemDataBuilder

object DataFactories {
    open class DataFactory<S : IDataBuilder<*, *>> {
        protected var defaults: S.() -> Unit = {}
        fun defaults(value: S.() -> Unit) {
            defaults = value
        }

        fun <S1 : S> build(builder: S1, block: S1.() -> Unit) {
            builder.apply {
                defaults()
                block()
                build()
            }
        }
    }

    class ItemDataFactory : DataFactory<IItemDataBuilder<*, *>>() {
        fun <U : Item> item(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
            build(DATA_GEN.item(entry), block)
        }
    }

    fun itemData(block: ItemDataFactory.() -> Unit) {
        ItemDataFactory().apply(block)
    }

    fun <U : Item> itemData(entry: IEntry<U>, block: IItemDataBuilder<U, *>.() -> Unit) {
        itemData { item(entry, block) }
    }

    class BlockDataFactory : DataFactory<IBlockDataBuilder<*, *>>() {
        fun <U : Block> block(entry: IEntry<U>, block: IBlockDataBuilder<U, *>.() -> Unit) {
            build(DATA_GEN.block(entry), block)
        }
    }

    fun blockData(block: BlockDataFactory.() -> Unit) {
        BlockDataFactory().apply(block)
    }

    fun <U : Block> blockData(entry: IEntry<U>, block: IBlockDataBuilder<U, *>.() -> Unit) {
        blockData { block(entry, block) }
    }

    fun dataGen(block: IDataGen.() -> Unit) {
        DATA_GEN.apply(block)
    }
}
