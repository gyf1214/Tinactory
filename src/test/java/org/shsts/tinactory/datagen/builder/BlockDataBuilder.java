package org.shsts.tinactory.datagen.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.context.RegistryDataContext;
import org.shsts.tinactory.datagen.handler.LootTableHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockDataBuilder<U extends Block, P> extends
        TrackedDataBuilder<Block, U, P, BlockDataBuilder<U, P>> {
    @Nullable
    private Consumer<RegistryDataContext<Block, U, BlockStateProvider>> blockState = null;
    @Nullable
    private Consumer<RegistryDataContext<Item, ?, ItemModelProvider>> itemModel = null;
    private boolean dropSet = false;

    public BlockDataBuilder(DataGen dataGen, P parent, ResourceLocation loc, Supplier<U> object) {
        super(dataGen, parent, loc, dataGen.blockTrackedCtx, object);
    }

    public BlockDataBuilder<U, P>
    blockState(Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cons) {
        this.blockState = cons;
        return this;
    }

    public <R, S extends BuilderBase<R, BlockDataBuilder<U, P>, S>>
    S blockState(Function<BlockDataBuilder<U, P>, S> builderFactory,
                 Function<R, Consumer<RegistryDataContext<Block, U, BlockStateProvider>>> consFunction) {
        var childBuilder = builderFactory.apply(this);
        childBuilder.onCreateObject(model -> this.blockState = consFunction.apply(model));
        return childBuilder;
    }

    public <U1 extends BlockItem> BlockDataBuilder<U, P>
    itemModel(Consumer<RegistryDataContext<Item, U1, ItemModelProvider>> cons) {
        this.itemModel = ctx -> cons.accept(ctx.convert());
        return this;
    }

    @SafeVarargs
    public final BlockDataBuilder<U, P> tag(TagKey<Block>... tags) {
        callbacks.add(() -> dataGen.tag(object, tags));
        return this;
    }

    @SafeVarargs
    public final BlockDataBuilder<U, P> itemTag(TagKey<Item>... tags) {
        callbacks.add(() -> dataGen.tag(() -> object.get().asItem(), tags));
        return this;
    }

    private LootTableHandler getDrop() {
        dropSet = true;
        return dataGen.lootTableHandler;
    }

    public BlockDataBuilder<U, P> drop(Supplier<? extends ItemLike> item, float chance) {
        getDrop().dropSingle(loc, item, chance);
        dropSet = true;
        return self();
    }

    public BlockDataBuilder<U, P> drop(Supplier<? extends ItemLike> item) {
        return drop(item, 1f);
    }

    public BlockDataBuilder<U, P> dropOnState(Supplier<? extends ItemLike> item,
                                              BooleanProperty prop, boolean value) {
        getDrop().dropOnState(loc, item, object, prop, value);
        dropSet = true;
        return self();
    }

    public BlockDataBuilder<U, P> dropSelf() {
        return drop(() -> object.get().asItem());
    }

    public BlockDataBuilder<U, P> dropSelfOnTool(TagKey<Item> tool) {
        getDrop().dropOnTool(loc, () -> object.get().asItem(), tool);
        dropSet = true;
        return self();
    }

    @Override
    protected void doRegister() {
        assert blockState != null;
        if (itemModel == null) {
            itemModel = ctx -> ctx.provider.withExistingParent(ctx.id,
                    new ResourceLocation(ctx.modid, "block/" + ctx.id));
        }
        if (!dropSet) {
            dropSelf();
        }
        dataGen.blockStateHandler.addBlockStateCallback(loc, object, blockState);
        dataGen.itemModelHandler.addBlockItemCallback(loc, object, ctx -> itemModel.accept(ctx));
    }
}
