package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.DistLazy;
import org.shsts.tinactory.registrate.IBlockParent;
import org.shsts.tinactory.registrate.IItemParent;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBuilder<U extends Block, P extends IBlockParent & IItemParent, S extends BlockBuilder<U, P, S>>
        extends RegistryEntryBuilder<Block, U, P, S> implements IItemParent {
    @Nullable
    protected Function<BlockBehaviour.Properties, U> factory = null;
    @Nullable
    protected Material material = null;
    protected Transformer<BlockBehaviour.Properties> properties = $ -> $;

    @Nullable
    protected Consumer<RegistryDataContext<Block, U, BlockStateProvider>> blockStateCallback = null;
    @Nullable
    protected Consumer<RegistryDataContext<Item, ? extends BlockItem, ItemModelProvider>> itemModelCallback = null;
    @Nullable
    protected DistLazy<BlockColor> tint = null;

    public BlockBuilder(Registrate registrate, String id, P parent,
                        Function<BlockBehaviour.Properties, U> factory) {
        super(registrate, registrate.blockHandler, id, parent);
        this.factory = factory;
    }

    protected BlockBuilder(Registrate registrate, String id, P parent) {
        super(registrate, registrate.blockHandler, id, parent);
    }

    public S material(Material material) {
        this.material = material;
        return self();
    }

    public S properties(Transformer<BlockBehaviour.Properties> trans) {
        this.properties = this.properties.chain(trans);
        return self();
    }

    public S renderType(DistLazy<RenderType> renderType) {
        this.onCreateObject.add(block -> renderType.runOnDist(Dist.CLIENT, () -> type ->
                this.registrate.renderTypeHandler.setRenderType(block, type)));
        return self();
    }

    public S translucent() {
        return this.renderType(() -> RenderType::translucent);
    }

    public S tint(DistLazy<BlockColor> color) {
        this.tint = color;
        return self();
    }

    public S tint(int... colors) {
        return this.tint(() -> () -> ($1, $2, $3, index) -> index < colors.length ? colors[index] : 0xFFFFFFFF);
    }

    @SafeVarargs
    public final S tag(TagKey<Block>... tags) {
        this.onCreateObject.add(entry -> this.registrate.tag(entry, tags));
        return self();
    }

    public Optional<DistLazy<ItemColor>> getItemTint() {
        var tint = this.tint;
        return tint == null ? Optional.empty() : Optional.of(() -> () -> (itemStack, index) -> {
            var item = (BlockItem) itemStack.getItem();
            return tint.getValue().getColor(item.getBlock().defaultBlockState(), null, null, index);
        });
    }

    public S blockState(Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cons) {
        this.blockStateCallback = cons;
        return self();
    }

    public <U1 extends BlockItem> S itemModel(Consumer<RegistryDataContext<Item, U1, ItemModelProvider>> cons) {
        this.itemModelCallback = ctx -> cons.accept(ctx.cast());
        return self();
    }

    public <U1 extends BlockItem> Consumer<RegistryDataContext<Item, U1, ItemModelProvider>>
    getItemModel() {
        if (this.itemModelCallback != null) {
            var ret = this.itemModelCallback;
            this.itemModelCallback = null;
            return ret::accept;
        } else {
            return ctx -> ctx.provider.withExistingParent(ctx.id,
                    new ResourceLocation(ctx.modid, "block/" + ctx.id));
        }
    }

    private class SimpleBlockItemBuilder<U1 extends BlockItem>
            extends BlockItemBuilder<U1, S, SimpleBlockItemBuilder<U1>> {
        public SimpleBlockItemBuilder(Factory<U1> factory) {
            super(BlockBuilder.this.registrate, BlockBuilder.this.self(), factory);
        }
    }

    public <U1 extends BlockItem> BlockItemBuilder<U1, S, ?> blockItem(BlockItemBuilder.Factory<U1> factory) {
        return new SimpleBlockItemBuilder<>(factory);
    }

    public BlockItemBuilder<BlockItem, S, ?> blockItem() {
        return blockItem(BlockItem::new);
    }

    public S defaultBlockItem() {
        return this.blockItem().build();
    }

    public S drop(Supplier<? extends Item> item) {
        var loc = ModelGen.prepend(this.loc, "blocks");
        this.registrate.lootTableHandler.blockLoot(loot -> loot.dropSingle(loc, item.get()));
        return self();
    }

    public S dropSelf() {
        var loc = ModelGen.prepend(this.loc, "blocks");
        this.onCreateEntry.add(block ->
                this.registrate.lootTableHandler.blockLoot(loot -> loot.dropSingle(loc, block.get())));
        return self();
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        if (this.blockStateCallback != null) {
            this.addDataCallback(this.registrate.blockStateHandler, this.blockStateCallback);
        }
        var tint = this.tint;
        if (tint != null) {
            this.onCreateObject.add(block -> tint.runOnDist(Dist.CLIENT, () -> blockColor ->
                    this.registrate.tintHandler.addBlockColor(block, blockColor)));
        }
        return super.createEntry();
    }

    @Override
    public U createObject() {
        var material = this.material == null ? this.parent.getDefaultMaterial() : this.material;
        var properties = this.properties.apply(
                this.parent.getDefaultBlockProperties().apply(BlockBehaviour.Properties.of(material)));
        assert this.factory != null;
        return this.factory.apply(properties);
    }

    @Nullable
    @Override
    public CreativeModeTab getDefaultCreativeModeTab() {
        return this.parent.getDefaultCreativeModeTab();
    }

    @Override
    public Transformer<Item.Properties> getDefaultItemProperties() {
        return this.parent.getDefaultItemProperties();
    }
}
