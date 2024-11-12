package org.shsts.tinactory.registrate.builder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BlockEntityBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>, P>
    extends SimpleBuilder<RegistryEntry<U>, P, BlockEntityBuilder<T, U, P>> {
    private final ValueHolder<Supplier<U>> blockHolder = ValueHolder.create();
    private final ValueHolder<Supplier<SmartBlockEntityType<T>>> typeHolder =
        ValueHolder.create();

    @Nullable
    private BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U, P>> typeBuilder = null;
    @Nullable
    private EntityBlockBuilder<T, U, BlockEntityBuilder<T, U, P>> blockBuilder = null;

    public BlockEntityBuilder(P parent) {
        super(parent);
    }

    protected abstract BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U, P>> createBlockEntityBuilder();

    protected abstract EntityBlockBuilder<T, U, BlockEntityBuilder<T, U, P>> createBlockBuilder();

    public BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U, P>> blockEntity() {
        if (typeBuilder == null) {
            var blockHolder = this.blockHolder;
            typeBuilder = this.createBlockEntityBuilder()
                .validBlock(() -> blockHolder.get().get());
        }
        return typeBuilder;
    }

    public EntityBlockBuilder<T, U, BlockEntityBuilder<T, U, P>> block() {
        if (blockBuilder == null) {
            blockBuilder = createBlockBuilder().type(typeHolder);
        }
        return blockBuilder;
    }

    public BlockEntityBuilder<T, U, P> entityClass(Class<T> entityClass) {
        return blockEntity().entityClass(entityClass).build();
    }

    public BlockEntityBuilder<T, U, P> translucent() {
        return block().translucent().build();
    }

    @Override
    public RegistryEntry<U> createObject() {
        var block = this.block().register();
        var blockEntity = this.blockEntity().register();

        blockHolder.setValue(block);
        typeHolder.setValue(blockEntity);

        return block;
    }
}
