package org.shsts.tinactory.core.builder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IBlockEntityType;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockEntityBuilder<U extends SmartEntityBlock, P>
    extends SimpleBuilder<IEntry<U>, P, BlockEntityBuilder<U, P>> {
    private final IRegistrate registrate;
    private final String id;
    private final SmartEntityBlock.Factory<U> factory;
    private final ValueHolder<IEntry<U>> blockHolder = ValueHolder.create();
    private final ValueHolder<IBlockEntityType> typeHolder = ValueHolder.create();

    @Nullable
    private IMenuType menu = null;
    @Nullable
    private IBlockEntityTypeBuilder<BlockEntityBuilder<U, P>> typeBuilder = null;
    @Nullable
    private IBlockBuilder<U, BlockEntityBuilder<U, P>> blockBuilder = null;

    public BlockEntityBuilder(IRegistrate registrate, P parent, String id,
        SmartEntityBlock.Factory<U> factory) {
        super(parent);
        this.registrate = registrate;
        this.id = id;
        this.factory = factory;
    }

    protected IBlockEntityTypeBuilder<BlockEntityBuilder<U, P>> createTypeBuilder() {
        return registrate.blockEntityType(this, id)
            .validBlock(() -> blockHolder.get().get());
    }

    protected IBlockBuilder<U, BlockEntityBuilder<U, P>> createBlockBuilder() {
        return registrate.block(this, id, properties ->
            factory.create(properties, typeHolder, menu));
    }

    public IBlockEntityTypeBuilder<BlockEntityBuilder<U, P>> blockEntity() {
        if (typeBuilder == null) {
            typeBuilder = createTypeBuilder();
        }
        return typeBuilder;
    }

    public IBlockBuilder<U, BlockEntityBuilder<U, P>> block() {
        if (blockBuilder == null) {
            blockBuilder = createBlockBuilder();
        }
        return blockBuilder;
    }

    public BlockEntityBuilder<U, P> menu(IMenuType value) {
        menu = value;
        return self();
    }

    public BlockEntityBuilder<U, P> translucent() {
        return block().translucent().end();
    }

    @Override
    public IEntry<U> createObject() {
        var block = this.block().register();
        var blockEntity = this.blockEntity().register();

        blockHolder.setValue(block);
        typeHolder.setValue(blockEntity);

        return block;
    }

    public static <U extends SmartEntityBlock, P> BlockEntityBuilder<U, P> builder(
        P parent, String id, SmartEntityBlock.Factory<U> factory) {
        return new BlockEntityBuilder<>(REGISTRATE, parent, id, factory);
    }

    public static <U extends SmartEntityBlock> BlockEntityBuilder<U, ?> builder(
        String id, SmartEntityBlock.Factory<U> factory) {
        return builder(Unit.INSTANCE, id, factory);
    }
}
