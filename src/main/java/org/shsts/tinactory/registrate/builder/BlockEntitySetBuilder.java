package org.shsts.tinactory.registrate.builder;

import org.shsts.tinactory.core.common.ISelf;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class BlockEntitySetBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>,
        R extends BlockEntitySet<T, U>, S extends BlockEntitySetBuilder<T, U, R, S>> implements ISelf<S> {
    protected final ValueHolder<Supplier<U>> blockHolder = ValueHolder.create();
    protected final ValueHolder<Supplier<SmartBlockEntityType<T>>> blockEntityHolder =
            ValueHolder.create();

    @Nullable
    protected BlockEntityBuilder<T, S> blockEntityBuilder = null;
    @Nullable
    protected EntityBlockBuilder<T, U, S> blockBuilder = null;

    protected abstract BlockEntityBuilder<T, S> createBlockEntityBuilder();

    protected abstract EntityBlockBuilder<T, U, S> createBlockBuilder();

    public BlockEntityBuilder<T, S> blockEntity() {
        if (this.blockEntityBuilder == null) {
            var blockHolder = this.blockHolder;
            this.blockEntityBuilder = this.createBlockEntityBuilder()
                    .validBlock(() -> blockHolder.get().get());
        }
        return this.blockEntityBuilder;
    }

    public EntityBlockBuilder<T, U, S> block() {
        if (this.blockBuilder == null) {
            var blockEntityHolder = this.blockEntityHolder;
            this.blockBuilder = this.createBlockBuilder()
                    .type(blockEntityHolder);
        }
        return this.blockBuilder;
    }

    protected abstract R createSet(RegistryEntry<SmartBlockEntityType<T>> blockEntity, RegistryEntry<U> block);

    public S entityClass(Class<T> entityClass) {
        this.blockEntity().entityClass(entityClass);
        return self();
    }

    public R register() {
        var block = this.block().register();
        var blockEntity = this.blockEntity().register();

        this.blockHolder.setValue(block);
        this.blockEntityHolder.setValue(blockEntity);

        return this.createSet(blockEntity, block);
    }
}
