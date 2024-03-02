package org.shsts.tinactory.core.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntitySet<T extends SmartBlockEntity, U extends SmartEntityBlock<T>> {
    public final RegistryEntry<SmartBlockEntityType<T>> blockEntityType;
    public final RegistryEntry<U> block;

    protected BlockEntitySet(RegistryEntry<SmartBlockEntityType<T>> blockEntityType, RegistryEntry<U> block) {
        this.blockEntityType = blockEntityType;
        this.block = block;
    }

    public U getBlock() {
        return this.block.get();
    }

    public abstract static class Builder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>,
            R extends BlockEntitySet<T, U>, S extends Builder<T, U, R, S>> implements ISelf<S> {
        protected final ValueHolder<Supplier<U>> blockHolder = ValueHolder.create();
        protected final ValueHolder<Supplier<SmartBlockEntityType<T>>> blockEntityHolder =
                ValueHolder.create();

        @Nullable
        protected BlockEntityBuilder<T, S, ?> blockEntityBuilder = null;
        @Nullable
        protected EntityBlockBuilder<T, U, S, ?> blockBuilder = null;

        protected abstract BlockEntityBuilder<T, S, ?> createBlockEntityBuilder();

        protected abstract EntityBlockBuilder<T, U, S, ?> createBlockBuilder();

        public BlockEntityBuilder<T, S, ?> blockEntity() {
            if (this.blockEntityBuilder == null) {
                var blockHolder = this.blockHolder;
                this.blockEntityBuilder = this.createBlockEntityBuilder()
                        .validBlock(() -> blockHolder.get().get());
            }
            return this.blockEntityBuilder;
        }

        public EntityBlockBuilder<T, U, S, ?> block() {
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

    private static class SimpleBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
            extends Builder<T, U, BlockEntitySet<T, U>, SimpleBuilder<T, U>> {
        private final Registrate registrate;
        private final String id;
        private final BlockEntityBuilder.Factory<T> blockEntityFactory;
        private final EntityBlockBuilder.Factory<T, U> blockFactory;

        private SimpleBuilder(Registrate registrate, String id,
                              BlockEntityBuilder.Factory<T> blockEntityFactory,
                              EntityBlockBuilder.Factory<T, U> blockFactory) {
            this.registrate = registrate;
            this.id = id;
            this.blockEntityFactory = blockEntityFactory;
            this.blockFactory = blockFactory;
        }

        @Override
        protected BlockEntityBuilder<T, SimpleBuilder<T, U>, ?> createBlockEntityBuilder() {
            return registrate.blockEntity(this, this.id, this.blockEntityFactory);
        }

        @Override
        protected EntityBlockBuilder<T, U, SimpleBuilder<T, U>, ?> createBlockBuilder() {
            return registrate.entityBlock(this, this.id, this.blockFactory);
        }

        @Override
        protected BlockEntitySet<T, U>
        createSet(RegistryEntry<SmartBlockEntityType<T>> blockEntity, RegistryEntry<U> block) {
            return new BlockEntitySet<>(blockEntity, block);
        }
    }

    public static <T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
    Builder<T, U, BlockEntitySet<T, U>, ?>
    builder(Registrate registrate, String id,
            BlockEntityBuilder.Factory<T> blockEntityFactory,
            EntityBlockBuilder.Factory<T, U> blockFactory) {
        return new SimpleBuilder<>(registrate, id, blockEntityFactory, blockFactory);
    }
}
