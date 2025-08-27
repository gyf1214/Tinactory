package org.shsts.tinactory.datagen.content.builder

import net.minecraft.world.level.block.Block
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder

class BlockDataFactory : DataFactory<IBlockDataBuilder<*, *>>() {
    fun <U : Block> block(entry: IEntry<U>, block: IBlockDataBuilder<U, *>.() -> Unit) {
        build(DATA_GEN.block(entry), block)
    }
}
