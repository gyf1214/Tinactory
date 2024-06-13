package org.shsts.tinactory.registrate.builder;

import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class BlockEntityBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>> {
    private final ValueHolder<Supplier<U>> blockHolder = ValueHolder.create();
    private final ValueHolder<Supplier<SmartBlockEntityType<T>>> typeHolder =
            ValueHolder.create();

    @Nullable
    private BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U>> typeBuilder = null;
    @Nullable
    private EntityBlockBuilder<T, U, BlockEntityBuilder<T, U>> blockBuilder = null;

    protected abstract BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U>> createBlockEntityBuilder();

    protected abstract EntityBlockBuilder<T, U, BlockEntityBuilder<T, U>> createBlockBuilder();

    public BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U>> blockEntity() {
        if (typeBuilder == null) {
            var blockHolder = this.blockHolder;
            typeBuilder = this.createBlockEntityBuilder()
                    .validBlock(() -> blockHolder.get().get());
        }
        return typeBuilder;
    }

    public EntityBlockBuilder<T, U, BlockEntityBuilder<T, U>> block() {
        if (blockBuilder == null) {
            blockBuilder = createBlockBuilder().type(typeHolder);
        }
        return blockBuilder;
    }

    public BlockEntityBuilder<T, U> entityClass(Class<T> entityClass) {
        return blockEntity().entityClass(entityClass).build();
    }

    public BlockEntityBuilder<T, U> translucent() {
        return block().translucent().build();
    }

    public RegistryEntry<U> register() {
        var block = this.block().register();
        var blockEntity = this.blockEntity().register();

        blockHolder.setValue(block);
        typeHolder.setValue(blockEntity);

        return block;
    }
}
