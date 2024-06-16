package org.shsts.tinactory.datagen.builder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.context.RegistryDataContext;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockDataBuilder<U extends Block, P> extends
        TrackedDataBuilder<Block, U, P, BlockDataBuilder<U, P>> {
    @Nullable
    private Consumer<RegistryDataContext<Block, U, BlockStateProvider>> blockState = null;
    @Nullable
    private Consumer<RegistryDataContext<Item, ? extends BlockItem, ItemModelProvider>> itemModel = null;
    private boolean dropSet = false;

    public BlockDataBuilder(DataGen dataGen, P parent, ResourceLocation loc, Supplier<U> object) {
        super(dataGen, parent, loc, dataGen.blockTrackedCtx, object);
    }

    public BlockDataBuilder<U, P>
    blockState(Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cons) {
        this.blockState = cons;
        return this;
    }

    public BlockDataBuilder<U, P>
    itemModel(Consumer<RegistryDataContext<Item, ? extends BlockItem, ItemModelProvider>> cons) {
        this.itemModel = cons;
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

    public BlockDataBuilder<U, P> drop(Supplier<? extends Item> item) {
        dataGen.lootTableHandler.dropSingle(loc, item);
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
            dataGen.lootTableHandler.dropSingle(loc, () -> object.get().asItem());
        }
        dataGen.blockStateHandler.addBlockStateCallback(loc, object, blockState);
        dataGen.itemModelHandler.addBlockItemCallback(loc, object, ctx -> itemModel.accept(ctx));
    }
}
