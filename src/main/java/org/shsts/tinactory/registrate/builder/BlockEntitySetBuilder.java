package org.shsts.tinactory.registrate.builder;

import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.registrate.common.BlockEntitySet;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class BlockEntitySetBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>> {
    private final ValueHolder<Supplier<U>> blockHolder = ValueHolder.create();
    private final ValueHolder<Supplier<SmartBlockEntityType<T>>> blockEntityHolder =
            ValueHolder.create();

    @Nullable
    private BlockEntityBuilder<T, BlockEntitySetBuilder<T, U>> blockEntityBuilder = null;
    @Nullable
    private EntityBlockBuilder<T, U, BlockEntitySetBuilder<T, U>> blockBuilder = null;

    protected abstract BlockEntityBuilder<T, BlockEntitySetBuilder<T, U>> createBlockEntityBuilder();

    protected abstract EntityBlockBuilder<T, U, BlockEntitySetBuilder<T, U>> createBlockBuilder();

    public BlockEntityBuilder<T, BlockEntitySetBuilder<T, U>> blockEntity() {
        if (this.blockEntityBuilder == null) {
            var blockHolder = this.blockHolder;
            this.blockEntityBuilder = this.createBlockEntityBuilder()
                    .validBlock(() -> blockHolder.get().get());
        }
        return this.blockEntityBuilder;
    }

    public EntityBlockBuilder<T, U, BlockEntitySetBuilder<T, U>> block() {
        if (this.blockBuilder == null) {
            var blockEntityHolder = this.blockEntityHolder;
            this.blockBuilder = this.createBlockBuilder()
                    .type(blockEntityHolder);
        }
        return this.blockBuilder;
    }

    public BlockEntitySetBuilder<T, U> entityClass(Class<T> entityClass) {
        this.blockEntity().entityClass(entityClass);
        return this;
    }

    public BlockEntitySet<T, U> register() {
        var block = this.block().register();
        var blockEntity = this.blockEntity().register();

        this.blockHolder.setValue(block);
        this.blockEntityHolder.setValue(blockEntity);

        return new BlockEntitySet<>(blockEntity, block);
    }
}
