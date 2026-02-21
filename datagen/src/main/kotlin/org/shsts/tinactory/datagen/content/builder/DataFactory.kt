package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinycorelib.datagen.api.builder.IDataBuilder

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
